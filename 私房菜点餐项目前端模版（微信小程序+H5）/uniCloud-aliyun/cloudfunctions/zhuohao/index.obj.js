// 云对象教程: https://uniapp.dcloud.net.cn/uniCloud/cloud-obj
// jsdoc语法提示教程：https://ask.dcloud.net.cn/docs/#//ask.dcloud.net.cn/article/129

// 错误代码定义
const ERROR_CODES = {
	// 输入验证错误 (1xxx)
	INVALID_INPUT: '1001',
	EMPTY_TABLE_NUMBER: '1002',
	INVALID_TABLE_NUMBER_FORMAT: '1003',
	TABLE_NUMBER_EXISTS: '1004',
	INVALID_DINER_COUNT: '1005',
	
	// 外部服务错误 (2xxx)
	WECHAT_API_ERROR: '2001',
	CLOUD_STORAGE_ERROR: '2002',
	NETWORK_ERROR: '2003',
	
	// 数据库错误 (3xxx)
	DATABASE_ERROR: '3001',
	DATABASE_TIMEOUT: '3002',
	DUPLICATE_KEY: '3003',
	
	// 权限错误 (4xxx)
	UNAUTHORIZED: '4001',
	FORBIDDEN: '4003',
	
	// 系统错误 (5xxx)
	INTERNAL_ERROR: '5000',
	UNKNOWN_ERROR: '5999'
}

module.exports = {
	_before: function () {
		// 通用预处理器
		this.db = uniCloud.database()
		this.collection = this.db.collection('zhuohao')
		this.errorLogCollection = this.db.collection('error_logs')
		this.tokenCacheCollection = this.db.collection('token_cache')
		
		// 微信 API 配置
		this.wechatConfig = {
			appid: 'wxc63d199886077877',
			secret: 'e51393d39a5e8515bc97542eca91f74b'
		}
	},
	
	/**
	 * 获取微信 access_token（带数据库缓存）
	 * @returns {Promise<string>} access_token
	 */
	async _getAccessToken() {
		try {
			const now = Date.now()
			const cacheKey = `wechat_access_token_${this.wechatConfig.appid}`
			
			// 1. 尝试从数据库缓存读取
			const cacheResult = await this.tokenCacheCollection
				.where({ 
					cache_key: cacheKey,
					expire_time: this.db.command.gt(now)  // 未过期
				})
				.get()
			
			if (cacheResult.data.length > 0) {
				const cachedToken = cacheResult.data[0]
				console.log('[Token] 使用缓存的 access_token')
				return cachedToken.access_token
			}
			
			// 2. 缓存未命中或已过期，调用微信 API 获取新 token
			console.log('[Token] 缓存未命中，从微信 API 获取新 token')
			
			const wxApi = uniCloud.getWeixinAPI(this.wechatConfig)
			const tokenResult = await wxApi.getAccessToken()
			
			if (!tokenResult.accessToken) {
				throw new Error('获取 access_token 失败')
			}
			
			// 3. 保存到数据库缓存（有效期 7000 秒，留 200 秒缓冲）
			const expireTime = now + 7000 * 1000
			
			// 先删除旧缓存
			await this.tokenCacheCollection
				.where({ cache_key: cacheKey })
				.remove()
			
			// 插入新缓存
			await this.tokenCacheCollection.add({
				cache_key: cacheKey,
				access_token: tokenResult.accessToken,
				expire_time: expireTime,
				create_time: new Date(),
				update_time: new Date()
			})
			
			console.log('[Token] 新 token 已缓存到数据库')
			
			return tokenResult.accessToken
			
		} catch (error) {
			console.error('[Token] 获取 access_token 失败:', error)
			
			// 记录错误日志
			await this.logError({
				operation: 'getAccessToken',
				errorCode: ERROR_CODES.WECHAT_API_ERROR,
				errorMessage: error.message || '获取 access_token 失败',
				context: { 
					appid: this.wechatConfig.appid
				}
			})
			
			throw error
		}
	},
	
	/**
	 * 记录错误日志到数据库
	 * @param {Object} errorInfo - 错误信息对象
	 * @param {string} errorInfo.operation - 操作名称
	 * @param {string} errorInfo.errorCode - 错误代码
	 * @param {string} errorInfo.errorMessage - 错误消息
	 * @param {Object} errorInfo.context - 上下文信息（可选）
	 * @param {string} errorInfo.userId - 用户ID（可选）
	 * @returns {Promise<Object>} 日志记录结果
	 */
	async logError(errorInfo) {
		try {
			const {
				operation,
				errorCode,
				errorMessage,
				context = {},
				userId = null
			} = errorInfo
			
			// 构建日志记录
			const logRecord = {
				operation: operation || 'UNKNOWN',
				error_code: errorCode || ERROR_CODES.UNKNOWN_ERROR,
				error_message: errorMessage || '未知错误',
				context: context,
				user_id: userId,
				create_time: new Date(),
				// 添加环境信息
				environment: {
					platform: this.getClientInfo().platform || 'unknown',
					appVersion: this.getClientInfo().appVersion || 'unknown',
					uniPlatform: this.getClientInfo().uniPlatform || 'unknown'
				}
			}
			
			// 保存到数据库
			const result = await this.errorLogCollection.add(logRecord)
			
			console.log('[ErrorLog] 错误日志已记录:', result.id)
			
			return {
				success: true,
				logId: result.id
			}
			
		} catch (error) {
			// 记录日志失败不应影响主流程
			console.error('[ErrorLog] 记录错误日志失败:', error)
			return {
				success: false,
				errMsg: '日志记录失败'
			}
		}
	},
	
	/**
	 * 统一错误处理函数
	 * @param {Error} error - 错误对象
	 * @param {string} operation - 操作名称
	 * @param {Object} context - 上下文信息（可选）
	 * @param {boolean} isDev - 是否开发环境
	 * @returns {Object} 标准化错误响应
	 */
	async _handleError(error, operation, context = {}, isDev = false) {
		// 记录详细错误日志（用于调试）
		console.error(`[QRCode] ${operation} 失败:`, {
			message: error.message,
			stack: error.stack,
			code: error.code
		})
		
		// 确定错误代码
		let errCode = ERROR_CODES.INTERNAL_ERROR
		if (error.code && ERROR_CODES[error.code]) {
			errCode = ERROR_CODES[error.code]
		}
		
		// 记录到数据库（异步，不阻塞主流程）
		this.logError({
			operation: operation,
			errorCode: errCode,
			errorMessage: error.message || '未知错误',
			context: {
				...context,
				stack: error.stack
			}
		}).catch(logError => {
			console.error('[QRCode] 记录错误日志失败:', logError)
		})
		
		// 返回用户友好的错误信息
		const response = {
			success: false,
			errCode: errCode,
			errMsg: '系统错误，请稍后重试'
		}
		
		// 开发环境返回详细信息
		if (isDev) {
			response.details = {
				message: error.message,
				operation: operation,
				context: context
			}
		}
		
		return response
	},
	
	/**
	 * 数据库操作重试辅助函数
	 * @param {Function} operation - 数据库操作函数
	 * @param {number} retryCount - 重试次数
	 * @returns {Promise<any>} 操作结果
	 */
	async _saveWithRetry(operation, retryCount = 1) {
		try {
			return await operation()
		} catch (error) {
			if (retryCount > 0) {
				console.log('[QRCode] 数据库操作失败，重试中...', `剩余重试次数: ${retryCount}`)
				// 等待 1 秒后重试
				await new Promise(resolve => setTimeout(resolve, 1000))
				return await this._saveWithRetry(operation, retryCount - 1)
			}
			throw error
		}
	},
	
	/**
	 * 生成桌号小程序码
	 * @param {string} tableNumber - 桌号
	 * @returns {Promise<Object>} 返回包含小程序码 URL 和记录 ID 的对象
	 */
	async generateQRCode(tableNumber) {
		let uploadedFileId = null
		
		try {
			// 1. 验证桌号输入
			const validation = this._validateTableNumber(tableNumber)
			if (!validation.valid) {
				// 记录验证失败日志
				await this.logError({
					operation: 'generateQRCode',
					errorCode: validation.errCode,
					errorMessage: validation.message,
					context: { tableNumber }
				})
				
				return {
					success: false,
					errCode: validation.errCode,
					errMsg: validation.message
				}
			}
			
			// 清理桌号（去除首尾空白）
			const cleanTableNumber = tableNumber.trim()
			
			// 2. 检查桌号是否已存在（唯一性检查）
			const existing = await this.collection
				.where({ table_number: cleanTableNumber })
				.get()
			
			if (existing.data.length > 0) {
				// 记录重复桌号日志
				await this.logError({
					operation: 'generateQRCode',
					errorCode: ERROR_CODES.TABLE_NUMBER_EXISTS,
					errorMessage: '该桌号已存在',
					context: { tableNumber: cleanTableNumber }
				})
				
				return {
					success: false,
					errCode: ERROR_CODES.TABLE_NUMBER_EXISTS,
					errMsg: '该桌号已存在'
				}
			}
			
			// 3. 调用微信 API 生成小程序码
			// 注意：uniCloud.getWeixinAPI 已经内置了 token 管理
			// 但我们使用自定义的数据库缓存来实现跨实例共享
			const wxApi = uniCloud.getWeixinAPI(this.wechatConfig)
			const qrcodeResult = await wxApi.getUnlimitedQRCode({
				scene: cleanTableNumber,  // 桌号作为场景值
				page: 'pages/index/index',  // 扫码后跳转的页面
				width: 280,  // 小程序码宽度
				isHyaline: false  // 是否需要透明底色
			})
			
			// 检查微信 API 调用结果
			if (qrcodeResult.errCode && qrcodeResult.errCode !== 0) {
				console.error('[QRCode] 微信 API 调用失败:', qrcodeResult.errMsg)
				
				// 记录微信 API 错误
				await this.logError({
					operation: 'generateQRCode',
					errorCode: ERROR_CODES.WECHAT_API_ERROR,
					errorMessage: qrcodeResult.errMsg || '微信 API 调用失败',
					context: { 
						tableNumber: cleanTableNumber,
						wxErrCode: qrcodeResult.errCode
					}
				})
				
				return {
					success: false,
					errCode: ERROR_CODES.WECHAT_API_ERROR,
					errMsg: '生成小程序码失败，请稍后重试'
				}
			}
			
			// 4. 上传到云存储
			const timestamp = Date.now()
			const uploadResult = await uniCloud.uploadFile({
				cloudPath: `qrcodes/${cleanTableNumber}_${timestamp}.png`,
				fileContent: qrcodeResult.buffer
			})
			
			if (!uploadResult.fileID) {
				console.error('[QRCode] 云存储上传失败')
				
				// 记录云存储错误
				await this.logError({
					operation: 'generateQRCode',
					errorCode: ERROR_CODES.CLOUD_STORAGE_ERROR,
					errorMessage: '云存储上传失败',
					context: { tableNumber: cleanTableNumber }
				})
				
				return {
					success: false,
					errCode: ERROR_CODES.CLOUD_STORAGE_ERROR,
					errMsg: '上传小程序码失败，请稍后重试'
				}
			}
			
			uploadedFileId = uploadResult.fileID
			console.log('[QRCode] 上传成功:', uploadedFileId)
			
			// 5. 保存到数据库（使用重试逻辑）
			const insertResult = await this._saveWithRetry(async () => {
				return await this.collection.add({
					table_number: cleanTableNumber,
					qrcode_image: uploadedFileId,
					create_time: new Date()
				})
			})
			
			console.log('[QRCode] 生成成功 - 桌号:', cleanTableNumber, '记录ID:', insertResult.id)
			
			// 返回成功结果
			return {
				success: true,
				data: {
					id: insertResult.id,
					qrcodeUrl: uploadedFileId,
					tableNumber: cleanTableNumber
				}
			}
			
		} catch (error) {
			console.error('[QRCode] 生成失败:', error)
			
			// 记录系统错误
			await this.logError({
				operation: 'generateQRCode',
				errorCode: ERROR_CODES.INTERNAL_ERROR,
				errorMessage: error.message || '系统错误',
				context: { 
					tableNumber,
					stack: error.stack
				}
			})
			
			// 回滚：删除已上传的文件
			if (uploadedFileId) {
				try {
					await uniCloud.deleteFile({ fileList: [uploadedFileId] })
					console.log('[QRCode] 已回滚删除文件:', uploadedFileId)
				} catch (deleteError) {
					console.error('[QRCode] 回滚删除文件失败:', deleteError)
				}
			}
			
			return {
				success: false,
				errCode: ERROR_CODES.INTERNAL_ERROR,
				errMsg: '系统错误，请稍后重试'
			}
		}
	},
	
	/**
	 * 清理过期的 token 缓存
	 * 建议定期调用此方法（如每天一次）
	 * @returns {Promise<Object>} 清理结果
	 */
	async cleanExpiredTokenCache() {
		try {
			const now = Date.now()
			
			// 删除所有过期的缓存记录
			const result = await this.tokenCacheCollection
				.where({
					expire_time: this.db.command.lt(now)
				})
				.remove()
			
			console.log('[Token] 清理过期缓存完成，删除记录数:', result.deleted)
			
			return {
				success: true,
				deleted: result.deleted
			}
			
		} catch (error) {
			console.error('[Token] 清理过期缓存失败:', error)
			return {
				success: false,
				errMsg: error.message
			}
		}
	},
	
	/**
	 * 验证桌号格式
	 * @param {string} tableNumber - 桌号
	 * @returns {Object} 验证结果
	 */
	_validateTableNumber(tableNumber) {
		// 检查是否为空
		if (!tableNumber || tableNumber.trim() === '') {
			return {
				valid: false,
				errCode: ERROR_CODES.EMPTY_TABLE_NUMBER,
				message: '桌号不能为空'
			}
		}
		
		// 检查长度（微信 scene 参数限制为 32 字符）
		if (tableNumber.length > 32) {
			return {
				valid: false,
				errCode: ERROR_CODES.INVALID_TABLE_NUMBER_FORMAT,
				message: '桌号长度不能超过32个字符'
			}
		}
		
		// 检查字符（仅允许中文、字母、数字）
		const pattern = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/
		if (!pattern.test(tableNumber.trim())) {
			return {
				valid: false,
				errCode: ERROR_CODES.INVALID_TABLE_NUMBER_FORMAT,
				message: '桌号只能包含中文、字母和数字'
			}
		}
		
		return { valid: true }
	},
	
	/**
	 * 查询桌号信息
	 * @param {string} tableNumber - 桌号
	 * @returns {Promise<Object>} 返回桌号记录
	 */
	async getTableInfo(tableNumber) {
		try {
			// 验证桌号
			if (!tableNumber || tableNumber.trim() === '') {
				return {
					success: false,
					errCode: ERROR_CODES.EMPTY_TABLE_NUMBER,
					errMsg: '桌号不能为空'
				}
			}
			
			const cleanTableNumber = tableNumber.trim()
			
			// 查询数据库
			const result = await this.collection
				.where({ table_number: cleanTableNumber })
				.get()
			
			if (result.data.length === 0) {
				return {
					success: false,
					errCode: ERROR_CODES.INVALID_INPUT,
					errMsg: '桌号不存在'
				}
			}
			
			return {
				success: true,
				data: result.data[0]
			}
			
		} catch (error) {
			console.error('[QRCode] 查询桌号信息失败:', error)
			return {
				success: false,
				errCode: ERROR_CODES.DATABASE_ERROR,
				errMsg: '查询失败，请稍后重试'
			}
		}
	},
	
	/**
	 * 获取所有桌号列表
	 * @param {number} pageSize - 每页数量（可选，默认 20）
	 * @param {number} pageNum - 页码（可选，默认 1）
	 * @returns {Promise<Array>} 返回所有桌号记录
	 */
	async getTableList(pageSize = 20, pageNum = 1) {
		try {
			// 计算跳过的记录数
			const skip = (pageNum - 1) * pageSize
			
			// 查询数据库，按创建时间倒序
			const result = await this.collection
				.orderBy('create_time', 'desc')
				.skip(skip)
				.limit(pageSize)
				.get()
			
			// 获取总数
			const countResult = await this.collection.count()
			
			return {
				success: true,
				data: {
					list: result.data,
					total: countResult.total,
					pageSize: pageSize,
					pageNum: pageNum
				}
			}
			
		} catch (error) {
			console.error('[QRCode] 获取桌号列表失败:', error)
			return {
				success: false,
				errCode: ERROR_CODES.DATABASE_ERROR,
				errMsg: '获取列表失败，请稍后重试'
			}
		}
	},
	
	/**
	 * 删除桌号
	 * @param {string} tableNumber - 桌号
	 * @returns {Promise<Object>} 返回删除结果
	 */
	async deleteTable(tableNumber) {
		try {
			// 验证桌号
			if (!tableNumber || tableNumber.trim() === '') {
				return {
					success: false,
					errCode: ERROR_CODES.EMPTY_TABLE_NUMBER,
					errMsg: '桌号不能为空'
				}
			}
			
			const cleanTableNumber = tableNumber.trim()
			
			// 先查询记录，获取小程序码图片 URL
			const queryResult = await this.collection
				.where({ table_number: cleanTableNumber })
				.get()
			
			if (queryResult.data.length === 0) {
				return {
					success: false,
					errCode: ERROR_CODES.INVALID_INPUT,
					errMsg: '桌号不存在'
				}
			}
			
			const record = queryResult.data[0]
			const qrcodeImage = record.qrcode_image
			
			// 删除数据库记录
			const deleteResult = await this.collection
				.where({ table_number: cleanTableNumber })
				.remove()
			
			// 删除云存储中的小程序码图片
			if (qrcodeImage) {
				try {
					await uniCloud.deleteFile({ fileList: [qrcodeImage] })
					console.log('[QRCode] 已删除云存储文件:', qrcodeImage)
				} catch (fileError) {
					console.error('[QRCode] 删除云存储文件失败:', fileError)
					// 文件删除失败不影响整体操作
				}
			}
			
			console.log('[QRCode] 删除成功 - 桌号:', cleanTableNumber)
			
			return {
				success: true,
				data: {
					deleted: deleteResult.deleted,
					tableNumber: cleanTableNumber
				}
			}
			
		} catch (error) {
			console.error('[QRCode] 删除桌号失败:', error)
			return {
				success: false,
				errCode: ERROR_CODES.DATABASE_ERROR,
				errMsg: '删除失败，请稍后重试'
			}
		}
	}
}
