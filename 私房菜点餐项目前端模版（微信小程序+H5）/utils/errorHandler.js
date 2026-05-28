/**
 * 错误处理工具
 * 提供统一的错误提示和用户引导
 */

/**
 * 错误类型枚举
 */
export const ErrorType = {
	NETWORK_TIMEOUT: 'NETWORK_TIMEOUT',      // 网络超时
	NETWORK_ERROR: 'NETWORK_ERROR',          // 网络错误
	INVALID_PARAM: 'INVALID_PARAM',          // 无效参数
	INVALID_TABLE: 'INVALID_TABLE',          // 无效桌号
	NO_TABLE: 'NO_TABLE',                    // 未确认桌号
	LOCATION_DENIED: 'LOCATION_DENIED',      // 位置权限被拒绝
	LOCATION_TIMEOUT: 'LOCATION_TIMEOUT',    // 定位超时
	STORAGE_ERROR: 'STORAGE_ERROR',          // 存储错误
	UPLOAD_ERROR: 'UPLOAD_ERROR',            // 上传失败
	UNKNOWN: 'UNKNOWN'                       // 未知错误
}

/**
 * 云对象实例（延迟初始化）
 */
let zhuohaoCloudObj = null

/**
 * 获取云对象实例
 */
function getCloudObject() {
	if (!zhuohaoCloudObj) {
		zhuohaoCloudObj = uniCloud.importObject('zhuohao')
	}
	return zhuohaoCloudObj
}

/**
 * 上报错误到云端
 * @param {string} operation - 操作名称
 * @param {string} errorCode - 错误代码
 * @param {string} errorMessage - 错误消息
 * @param {object} context - 上下文信息
 */
export async function logClientError(operation, errorCode, errorMessage, context = {}) {
	try {
		const cloudObj = getCloudObject()
		
		// 构建错误信息
		const errorInfo = {
			operation: operation,
			errorCode: errorCode,
			errorMessage: errorMessage,
			context: {
				...context,
				// 添加客户端信息
				timestamp: Date.now(),
				page: getCurrentPages().length > 0 ? getCurrentPages()[getCurrentPages().length - 1].route : 'unknown',
				// 添加系统信息
				systemInfo: uni.getSystemInfoSync()
			}
		}
		
		// 调用云函数记录错误
		const result = await cloudObj.logError(errorInfo)
		
		if (result.success) {
			console.log('[ErrorHandler] 错误已上报到云端:', result.logId)
		} else {
			console.error('[ErrorHandler] 错误上报失败:', result.errMsg)
		}
		
		return result
	} catch (error) {
		// 上报失败不应影响主流程
		console.error('[ErrorHandler] 错误上报异常:', error)
		return {
			success: false,
			errMsg: error.message
		}
	}
}

/**
 * 错误信息配置
 */
const ErrorMessages = {
	[ErrorType.NETWORK_TIMEOUT]: {
		title: '网络超时',
		content: '网络连接超时，请检查网络连接后重试',
		showCancel: true,
		confirmText: '重试'
	},
	[ErrorType.NETWORK_ERROR]: {
		title: '网络错误',
		content: '网络连接失败，请检查网络设置',
		showCancel: true,
		confirmText: '重试'
	},
	[ErrorType.INVALID_PARAM]: {
		title: '参数错误',
		content: '无法识别二维码信息，请重新扫描或联系服务员',
		showCancel: false,
		confirmText: '我知道了'
	},
	[ErrorType.INVALID_TABLE]: {
		title: '桌号无效',
		content: '该桌号不存在或已失效，请重新扫码或联系服务员',
		showCancel: true,
		cancelText: '取消',
		confirmText: '重新扫码'
	},
	[ErrorType.NO_TABLE]: {
		title: '未确认桌号',
		content: '请先扫描桌上的二维码确认桌号后再点餐',
		showCancel: true,
		cancelText: '取消',
		confirmText: '去扫码'
	},
	[ErrorType.LOCATION_DENIED]: {
		title: '需要位置权限',
		content: '为了给您提供更好的服务，需要获取您的位置信息',
		showCancel: true,
		cancelText: '暂不授权',
		confirmText: '去授权'
	},
	[ErrorType.LOCATION_TIMEOUT]: {
		title: '定位超时',
		content: '定位超时，请检查网络连接后重试',
		showCancel: true,
		confirmText: '重试'
	},
	[ErrorType.STORAGE_ERROR]: {
		title: '保存失败',
		content: '数据保存失败，请重试或联系服务员',
		showCancel: true,
		confirmText: '重试'
	},
	[ErrorType.UPLOAD_ERROR]: {
		title: '上传失败',
		content: '文件上传失败，请检查网络后重试',
		showCancel: true,
		confirmText: '重试'
	},
	[ErrorType.UNKNOWN]: {
		title: '操作失败',
		content: '操作失败，请稍后重试',
		showCancel: true,
		confirmText: '重试'
	}
}

/**
 * 显示错误提示
 * @param {string} errorType - 错误类型
 * @param {function} onConfirm - 确认回调
 * @param {function} onCancel - 取消回调
 * @param {string} customContent - 自定义错误内容
 */
export function showError(errorType, onConfirm, onCancel, customContent) {
	const config = ErrorMessages[errorType] || ErrorMessages[ErrorType.UNKNOWN]
	
	uni.showModal({
		title: config.title,
		content: customContent || config.content,
		showCancel: config.showCancel,
		cancelText: config.cancelText || '取消',
		confirmText: config.confirmText || '确定',
		success: (res) => {
			if (res.confirm && onConfirm) {
				onConfirm()
			} else if (res.cancel && onCancel) {
				onCancel()
			}
		}
	})
}

/**
 * 显示成功提示
 * @param {string} message - 提示信息
 * @param {number} duration - 持续时间（毫秒）
 */
export function showSuccess(message, duration = 2000) {
	uni.showToast({
		title: message,
		icon: 'success',
		duration: duration
	})
}

/**
 * 显示警告提示
 * @param {string} message - 提示信息
 * @param {number} duration - 持续时间（毫秒）
 */
export function showWarning(message, duration = 2000) {
	uni.showToast({
		title: message,
		icon: 'none',
		duration: duration
	})
}

/**
 * 显示加载提示
 * @param {string} message - 提示信息
 */
export function showLoading(message = '加载中...') {
	uni.showLoading({
		title: message,
		mask: true
	})
}

/**
 * 隐藏加载提示
 */
export function hideLoading() {
	uni.hideLoading()
}

/**
 * 解析错误对象，返回错误类型
 * @param {Error|object} error - 错误对象
 * @returns {string} 错误类型
 */
export function parseError(error) {
	if (!error) {
		return ErrorType.UNKNOWN
	}
	
	// 检查错误消息
	const errMsg = error.errMsg || error.message || ''
	
	if (errMsg.includes('timeout')) {
		return ErrorType.NETWORK_TIMEOUT
	}
	
	if (errMsg.includes('network') || errMsg.includes('fail')) {
		return ErrorType.NETWORK_ERROR
	}
	
	if (errMsg.includes('location') || errMsg.includes('getLocation')) {
		if (errMsg.includes('timeout')) {
			return ErrorType.LOCATION_TIMEOUT
		}
		return ErrorType.LOCATION_DENIED
	}
	
	if (errMsg.includes('storage') || errMsg.includes('setStorage')) {
		return ErrorType.STORAGE_ERROR
	}
	
	if (errMsg.includes('upload')) {
		return ErrorType.UPLOAD_ERROR
	}
	
	return ErrorType.UNKNOWN
}

/**
 * 统一错误处理函数
 * @param {Error|object} error - 错误对象
 * @param {function} onRetry - 重试回调
 * @param {string} customMessage - 自定义错误消息
 * @param {string} operation - 操作名称（用于错误上报）
 * @param {boolean} shouldReport - 是否上报到云端（默认 true）
 */
export function handleError(error, onRetry, customMessage, operation = 'unknown', shouldReport = true) {
	console.error('[ErrorHandler] 错误:', error)
	
	const errorType = parseError(error)
	
	// 上报关键错误到云端
	if (shouldReport && isKeyError(errorType)) {
		logClientError(
			operation,
			errorType,
			error.message || error.errMsg || customMessage || '未知错误',
			{
				errorType: errorType,
				stack: error.stack
			}
		).catch(err => {
			console.error('[ErrorHandler] 错误上报失败:', err)
		})
	}
	
	showError(errorType, onRetry, null, customMessage)
}

/**
 * 判断是否为关键错误（需要上报）
 * @param {string} errorType - 错误类型
 * @returns {boolean} 是否为关键错误
 */
function isKeyError(errorType) {
	// 以下错误类型需要上报
	const keyErrors = [
		ErrorType.NETWORK_ERROR,
		ErrorType.STORAGE_ERROR,
		ErrorType.UPLOAD_ERROR,
		ErrorType.UNKNOWN
	]
	
	return keyErrors.includes(errorType)
}

/**
 * 用户引导提示
 */
export const UserGuide = {
	/**
	 * 扫码引导
	 */
	scanGuide() {
		uni.showModal({
			title: '如何扫码点餐',
			content: '1. 找到桌上的二维码\n2. 使用微信扫一扫\n3. 确认桌号和人数\n4. 开始点餐',
			showCancel: false,
			confirmText: '我知道了'
		})
	},
	
	/**
	 * 首次使用引导
	 */
	firstTimeGuide() {
		uni.showModal({
			title: '欢迎使用',
			content: '扫描桌上的二维码即可开始点餐，享受便捷的用餐体验',
			showCancel: false,
			confirmText: '开始使用'
		})
	},
	
	/**
	 * 点餐流程引导
	 */
	orderGuide() {
		uni.showModal({
			title: '点餐流程',
			content: '1. 选择菜品加入购物车\n2. 点击购物车查看已选菜品\n3. 确认无误后去结算\n4. 完成支付等待上菜',
			showCancel: false,
			confirmText: '我知道了'
		})
	}
}
