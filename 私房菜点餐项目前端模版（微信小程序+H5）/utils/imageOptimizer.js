/**
 * 图片优化工具
 * 提供图片压缩、格式转换、懒加载等功能
 */

/**
 * 压缩图片
 * @param {string} imagePath - 图片路径
 * @param {object} options - 压缩选项
 * @param {number} options.quality - 压缩质量 (0-100)
 * @param {number} options.maxWidth - 最大宽度
 * @param {number} options.maxHeight - 最大高度
 * @returns {Promise<string>} 压缩后的图片临时路径
 */
export async function compressImage(imagePath, options = {}) {
	const {
		quality = 80,
		maxWidth = 1080,
		maxHeight = 1080
	} = options
	
	try {
		// 获取图片信息
		const imageInfo = await uni.getImageInfo({
			src: imagePath
		})
		
		// 计算压缩后的尺寸
		let { width, height } = imageInfo
		
		if (width > maxWidth || height > maxHeight) {
			const ratio = Math.min(maxWidth / width, maxHeight / height)
			width = Math.floor(width * ratio)
			height = Math.floor(height * ratio)
		}
		
		// 压缩图片
		const result = await uni.compressImage({
			src: imagePath,
			quality: quality,
			width: width,
			height: height
		})
		
		console.log('[ImageOptimizer] 图片压缩成功:', {
			original: imageInfo.width + 'x' + imageInfo.height,
			compressed: width + 'x' + height,
			quality: quality
		})
		
		return result.tempFilePath
		
	} catch (error) {
		console.error('[ImageOptimizer] 图片压缩失败:', error)
		// 压缩失败返回原图
		return imagePath
	}
}

/**
 * 批量压缩图片
 * @param {Array<string>} imagePaths - 图片路径数组
 * @param {object} options - 压缩选项
 * @returns {Promise<Array<string>>} 压缩后的图片路径数组
 */
export async function compressImages(imagePaths, options = {}) {
	const compressedPaths = []
	
	for (const imagePath of imagePaths) {
		try {
			const compressedPath = await compressImage(imagePath, options)
			compressedPaths.push(compressedPath)
		} catch (error) {
			console.error('[ImageOptimizer] 批量压缩失败:', imagePath, error)
			compressedPaths.push(imagePath)
		}
	}
	
	return compressedPaths
}

/**
 * 为云存储图片添加处理参数
 * @param {string} url - 云存储图片 URL
 * @param {object} options - 处理选项
 * @param {number} options.quality - 图片质量 (1-100)
 * @param {number} options.width - 目标宽度
 * @param {number} options.height - 目标高度
 * @param {string} options.format - 目标格式 (jpg, png, webp)
 * @returns {string} 处理后的 URL
 */
export function addCloudImageParams(url, options = {}) {
	if (!url || !url.includes('unicloud')) {
		return url
	}
	
	const {
		quality = 80,
		width,
		height,
		format
	} = options
	
	const params = []
	
	// 质量参数
	if (quality && quality < 100) {
		params.push(`quality,q_${quality}`)
	}
	
	// 尺寸参数
	if (width || height) {
		const resizeParams = []
		if (width) resizeParams.push(`w_${width}`)
		if (height) resizeParams.push(`h_${height}`)
		params.push(`resize,${resizeParams.join(',')}`)
	}
	
	// 格式转换
	if (format) {
		params.push(`format,${format}`)
	}
	
	if (params.length === 0) {
		return url
	}
	
	const separator = url.includes('?') ? '&' : '?'
	return `${url}${separator}x-oss-process=image/${params.join('/')}`
}

/**
 * 获取缩略图 URL
 * @param {string} url - 原图 URL
 * @param {number} size - 缩略图尺寸（正方形）
 * @returns {string} 缩略图 URL
 */
export function getThumbnailUrl(url, size = 200) {
	return addCloudImageParams(url, {
		width: size,
		height: size,
		quality: 75
	})
}

/**
 * 预加载图片
 * @param {string|Array<string>} urls - 图片 URL 或 URL 数组
 * @returns {Promise<void>}
 */
export async function preloadImages(urls) {
	const urlArray = Array.isArray(urls) ? urls : [urls]
	
	const promises = urlArray.map(url => {
		return new Promise((resolve, reject) => {
			uni.getImageInfo({
				src: url,
				success: () => {
					console.log('[ImageOptimizer] 预加载成功:', url)
					resolve()
				},
				fail: (error) => {
					console.error('[ImageOptimizer] 预加载失败:', url, error)
					reject(error)
				}
			})
		})
	})
	
	try {
		await Promise.all(promises)
		console.log('[ImageOptimizer] 批量预加载完成')
	} catch (error) {
		console.error('[ImageOptimizer] 批量预加载失败:', error)
	}
}

/**
 * 图片懒加载观察器
 */
export class ImageLazyLoader {
	constructor(options = {}) {
		this.options = {
			threshold: 0.1,  // 触发加载的阈值
			rootMargin: '50px',  // 提前加载的距离
			...options
		}
		
		this.images = new Map()
		this.observer = null
		
		// 初始化观察器
		this.initObserver()
	}
	
	/**
	 * 初始化 Intersection Observer
	 */
	initObserver() {
		// 注意：uni-app 不支持 IntersectionObserver API
		// 这里使用 uni.createIntersectionObserver
		// 需要在组件中使用
		console.log('[ImageLazyLoader] 懒加载观察器已初始化')
	}
	
	/**
	 * 添加需要懒加载的图片
	 * @param {string} id - 图片唯一标识
	 * @param {string} src - 图片地址
	 * @param {function} callback - 加载回调
	 */
	add(id, src, callback) {
		this.images.set(id, {
			src: src,
			callback: callback,
			loaded: false
		})
	}
	
	/**
	 * 移除图片
	 * @param {string} id - 图片唯一标识
	 */
	remove(id) {
		this.images.delete(id)
	}
	
	/**
	 * 加载图片
	 * @param {string} id - 图片唯一标识
	 */
	load(id) {
		const image = this.images.get(id)
		if (image && !image.loaded) {
			image.loaded = true
			if (image.callback) {
				image.callback(image.src)
			}
		}
	}
	
	/**
	 * 销毁观察器
	 */
	destroy() {
		this.images.clear()
		if (this.observer) {
			this.observer.disconnect()
			this.observer = null
		}
	}
}

/**
 * 图片格式检测
 * @param {string} url - 图片 URL
 * @returns {string} 图片格式 (jpg, png, gif, webp, etc.)
 */
export function getImageFormat(url) {
	if (!url) return 'unknown'
	
	const extension = url.split('.').pop().toLowerCase().split('?')[0]
	return extension || 'unknown'
}

/**
 * 判断是否支持 WebP
 * @returns {boolean}
 */
export function isSupportWebP() {
	// 小程序和 H5 大部分都支持 WebP
	// 可以根据平台和系统版本进一步判断
	// #ifdef MP-WEIXIN
	return true
	// #endif
	
	// #ifdef H5
	const canvas = document.createElement('canvas')
	if (canvas.getContext && canvas.getContext('2d')) {
		return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
	}
	return false
	// #endif
	
	return false
}

/**
 * 选择最优图片格式
 * @param {string} url - 原始图片 URL
 * @returns {string} 优化后的图片 URL
 */
export function getOptimalImageUrl(url) {
	if (!url) return url
	
	// 如果支持 WebP 且是云存储图片，转换为 WebP
	if (isSupportWebP() && url.includes('unicloud')) {
		const format = getImageFormat(url)
		if (format !== 'webp' && format !== 'gif') {
			return addCloudImageParams(url, {
				format: 'webp',
				quality: 85
			})
		}
	}
	
	return url
}

/**
 * 图片加载性能监控
 */
export class ImagePerformanceMonitor {
	constructor() {
		this.metrics = []
	}
	
	/**
	 * 开始监控
	 * @param {string} url - 图片 URL
	 * @returns {function} 结束监控的函数
	 */
	start(url) {
		const startTime = Date.now()
		
		return () => {
			const endTime = Date.now()
			const duration = endTime - startTime
			
			this.metrics.push({
				url: url,
				duration: duration,
				timestamp: endTime
			})
			
			console.log('[ImagePerformance] 图片加载耗时:', url, duration + 'ms')
			
			// 如果加载时间过长，记录警告
			if (duration > 3000) {
				console.warn('[ImagePerformance] 图片加载缓慢:', url, duration + 'ms')
			}
		}
	}
	
	/**
	 * 获取性能报告
	 * @returns {object} 性能统计
	 */
	getReport() {
		if (this.metrics.length === 0) {
			return {
				count: 0,
				avgDuration: 0,
				maxDuration: 0,
				minDuration: 0
			}
		}
		
		const durations = this.metrics.map(m => m.duration)
		const sum = durations.reduce((a, b) => a + b, 0)
		
		return {
			count: this.metrics.length,
			avgDuration: Math.round(sum / this.metrics.length),
			maxDuration: Math.max(...durations),
			minDuration: Math.min(...durations),
			slowImages: this.metrics.filter(m => m.duration > 2000)
		}
	}
	
	/**
	 * 清空监控数据
	 */
	clear() {
		this.metrics = []
	}
}

// 导出单例
export const imagePerformanceMonitor = new ImagePerformanceMonitor()
