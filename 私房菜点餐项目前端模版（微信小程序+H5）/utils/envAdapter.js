/**
 * 环境适配器
 * 根据不同环境选择对应的 API 实现
 */

/**
 * 检测当前运行环境
 * @returns {string} 'mp-weixin' | 'h5' | 'app' | 'unknown'
 */
export function detectEnvironment() {
	// #ifdef MP-WEIXIN
	return 'mp-weixin'
	// #endif
	
	// #ifdef H5
	return 'h5'
	// #endif
	
	// #ifdef APP-PLUS
	return 'app'
	// #endif
	
	return 'unknown'
}

/**
 * 获取环境信息
 * @returns {object} 环境详细信息
 */
export function getEnvironmentInfo() {
	const env = detectEnvironment()
	const info = {
		platform: env,
		isWeixin: env === 'mp-weixin',
		isH5: env === 'h5',
		isApp: env === 'app',
		// 获取系统信息
		systemInfo: null
	}
	
	try {
		info.systemInfo = uni.getSystemInfoSync()
	} catch (error) {
		console.error('[EnvAdapter] 获取系统信息失败:', error)
	}
	
	return info
}

/**
 * 存储适配器
 * 提供统一的存储接口
 */
export const StorageAdapter = {
	/**
	 * 设置存储
	 * @param {string} key - 键名
	 * @param {any} value - 值
	 * @returns {boolean} 是否成功
	 */
	set(key, value) {
		try {
			// #ifdef MP-WEIXIN
			uni.setStorageSync(key, value)
			return true
			// #endif
			
			// #ifdef H5
			if (typeof localStorage !== 'undefined') {
				localStorage.setItem(key, JSON.stringify(value))
				return true
			}
			return false
			// #endif
			
			// 默认使用 uni API
			uni.setStorageSync(key, value)
			return true
		} catch (error) {
			console.error('[StorageAdapter] 设置存储失败:', error)
			return false
		}
	},
	
	/**
	 * 获取存储
	 * @param {string} key - 键名
	 * @returns {any} 值
	 */
	get(key) {
		try {
			// #ifdef MP-WEIXIN
			return uni.getStorageSync(key)
			// #endif
			
			// #ifdef H5
			if (typeof localStorage !== 'undefined') {
				const value = localStorage.getItem(key)
				try {
					return JSON.parse(value)
				} catch {
					return value
				}
			}
			return null
			// #endif
			
			// 默认使用 uni API
			return uni.getStorageSync(key)
		} catch (error) {
			console.error('[StorageAdapter] 获取存储失败:', error)
			return null
		}
	},
	
	/**
	 * 删除存储
	 * @param {string} key - 键名
	 * @returns {boolean} 是否成功
	 */
	remove(key) {
		try {
			// #ifdef MP-WEIXIN
			uni.removeStorageSync(key)
			return true
			// #endif
			
			// #ifdef H5
			if (typeof localStorage !== 'undefined') {
				localStorage.removeItem(key)
				return true
			}
			return false
			// #endif
			
			// 默认使用 uni API
			uni.removeStorageSync(key)
			return true
		} catch (error) {
			console.error('[StorageAdapter] 删除存储失败:', error)
			return false
		}
	},
	
	/**
	 * 清空存储
	 * @returns {boolean} 是否成功
	 */
	clear() {
		try {
			// #ifdef MP-WEIXIN
			uni.clearStorageSync()
			return true
			// #endif
			
			// #ifdef H5
			if (typeof localStorage !== 'undefined') {
				localStorage.clear()
				return true
			}
			return false
			// #endif
			
			// 默认使用 uni API
			uni.clearStorageSync()
			return true
		} catch (error) {
			console.error('[StorageAdapter] 清空存储失败:', error)
			return false
		}
	}
}

/**
 * 导航适配器
 * 提供统一的导航接口
 */
export const NavigationAdapter = {
	/**
	 * 跳转到页面
	 * @param {string} url - 页面路径
	 * @param {object} options - 跳转选项
	 */
	navigateTo(url, options = {}) {
		const env = detectEnvironment()
		
		// #ifdef MP-WEIXIN
		uni.navigateTo({
			url,
			...options
		})
		// #endif
		
		// #ifdef H5
		// H5 环境可能需要特殊处理
		uni.navigateTo({
			url,
			...options
		})
		// #endif
	},
	
	/**
	 * 跳转到 tabBar 页面
	 * @param {string} url - 页面路径
	 */
	switchTab(url) {
		uni.switchTab({ url })
	},
	
	/**
	 * 返回上一页
	 * @param {number} delta - 返回层数
	 */
	navigateBack(delta = 1) {
		uni.navigateBack({ delta })
	},
	
	/**
	 * 重定向到页面
	 * @param {string} url - 页面路径
	 */
	redirectTo(url) {
		uni.redirectTo({ url })
	},
	
	/**
	 * 重新加载页面
	 * @param {string} url - 页面路径
	 */
	reLaunch(url) {
		uni.reLaunch({ url })
	}
}

/**
 * 网络请求适配器
 * 提供统一的网络请求接口
 */
export const RequestAdapter = {
	/**
	 * 发起网络请求
	 * @param {object} config - 请求配置
	 * @returns {Promise} 请求结果
	 */
	request(config) {
		return new Promise((resolve, reject) => {
			const env = detectEnvironment()
			
			// #ifdef MP-WEIXIN
			uni.request({
				...config,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
			// #endif
			
			// #ifdef H5
			// H5 环境可以使用 axios 或 fetch
			uni.request({
				...config,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
			// #endif
		})
	},
	
	/**
	 * 上传文件
	 * @param {object} config - 上传配置
	 * @returns {Promise} 上传结果
	 */
	uploadFile(config) {
		return new Promise((resolve, reject) => {
			uni.uploadFile({
				...config,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
		})
	},
	
	/**
	 * 下载文件
	 * @param {object} config - 下载配置
	 * @returns {Promise} 下载结果
	 */
	downloadFile(config) {
		return new Promise((resolve, reject) => {
			uni.downloadFile({
				...config,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
		})
	}
}

/**
 * 扫码适配器
 * 提供统一的扫码接口
 */
export const ScanAdapter = {
	/**
	 * 扫描二维码/条形码
	 * @param {object} options - 扫码选项
	 * @returns {Promise} 扫码结果
	 */
	scanCode(options = {}) {
		return new Promise((resolve, reject) => {
			const env = detectEnvironment()
			
			// #ifdef MP-WEIXIN
			uni.scanCode({
				...options,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
			// #endif
			
			// #ifdef H5
			// H5 环境可能不支持扫码，需要提示用户
			uni.showToast({
				title: 'H5环境暂不支持扫码',
				icon: 'none'
			})
			reject(new Error('H5环境不支持扫码'))
			// #endif
		})
	}
}

/**
 * 分享适配器
 * 提供统一的分享接口
 */
export const ShareAdapter = {
	/**
	 * 分享到微信
	 * @param {object} options - 分享选项
	 */
	shareToWeixin(options = {}) {
		const env = detectEnvironment()
		
		// #ifdef MP-WEIXIN
		// 小程序使用 onShareAppMessage
		return {
			title: options.title || '七云菜馆',
			path: options.path || '/pages/index/index',
			imageUrl: options.imageUrl || ''
		}
		// #endif
		
		// #ifdef H5
		// H5 环境可以使用微信 JS-SDK
		console.log('[ShareAdapter] H5 分享:', options)
		// #endif
	}
}

/**
 * 支付适配器
 * 提供统一的支付接口
 */
export const PaymentAdapter = {
	/**
	 * 发起支付
	 * @param {object} paymentData - 支付数据
	 * @returns {Promise} 支付结果
	 */
	requestPayment(paymentData) {
		return new Promise((resolve, reject) => {
			const env = detectEnvironment()
			
			// #ifdef MP-WEIXIN
			uni.requestPayment({
				...paymentData,
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
			// #endif
			
			// #ifdef H5
			// H5 环境需要调用微信 JS-SDK 支付
			console.log('[PaymentAdapter] H5 支付:', paymentData)
			reject(new Error('H5支付需要单独实现'))
			// #endif
		})
	}
}

/**
 * 获取适配器
 * 根据功能类型返回对应的适配器
 * @param {string} type - 适配器类型
 * @returns {object} 适配器对象
 */
export function getAdapter(type) {
	const adapters = {
		storage: StorageAdapter,
		navigation: NavigationAdapter,
		request: RequestAdapter,
		scan: ScanAdapter,
		share: ShareAdapter,
		payment: PaymentAdapter
	}
	
	return adapters[type] || null
}

/**
 * 检查功能是否支持
 * @param {string} feature - 功能名称
 * @returns {boolean} 是否支持
 */
export function isFeatureSupported(feature) {
	const env = detectEnvironment()
	
	const featureSupport = {
		'mp-weixin': {
			scan: true,
			payment: true,
			share: true,
			location: true,
			camera: true,
			bluetooth: true
		},
		'h5': {
			scan: false,
			payment: false, // 需要 JS-SDK
			share: false,   // 需要 JS-SDK
			location: true,
			camera: true,
			bluetooth: false
		},
		'app': {
			scan: true,
			payment: true,
			share: true,
			location: true,
			camera: true,
			bluetooth: true
		}
	}
	
	return featureSupport[env]?.[feature] || false
}

// 导出默认对象
export default {
	detectEnvironment,
	getEnvironmentInfo,
	StorageAdapter,
	NavigationAdapter,
	RequestAdapter,
	ScanAdapter,
	ShareAdapter,
	PaymentAdapter,
	getAdapter,
	isFeatureSupported
}
