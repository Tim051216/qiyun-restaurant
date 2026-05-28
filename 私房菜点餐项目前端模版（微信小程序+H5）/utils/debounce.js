/**
 * 防抖节流工具
 * 用于优化高频触发的事件处理
 */

/**
 * 防抖函数
 * 在事件被触发 n 秒后再执行回调，如果在这 n 秒内又被触发，则重新计时
 * 
 * @param {Function} func - 需要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @param {boolean} immediate - 是否立即执行（首次触发时）
 * @returns {Function} 防抖后的函数
 * 
 * @example
 * // 搜索输入框防抖
 * const debouncedSearch = debounce((keyword) => {
 *   console.log('搜索:', keyword)
 * }, 500)
 * 
 * // 在 input 事件中使用
 * onInput(e) {
 *   debouncedSearch(e.detail.value)
 * }
 */
export function debounce(func, wait = 300, immediate = false) {
	let timeout
	
	return function(...args) {
		const context = this
		
		// 清除之前的定时器
		if (timeout) {
			clearTimeout(timeout)
		}
		
		// 立即执行模式
		if (immediate) {
			const callNow = !timeout
			timeout = setTimeout(() => {
				timeout = null
			}, wait)
			
			if (callNow) {
				func.apply(context, args)
			}
		} else {
			// 延迟执行模式
			timeout = setTimeout(() => {
				func.apply(context, args)
			}, wait)
		}
	}
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 * 
 * @param {Function} func - 需要节流的函数
 * @param {number} wait - 等待时间（毫秒）
 * @param {Object} options - 配置选项
 * @param {boolean} options.leading - 是否在开始时执行（默认 true）
 * @param {boolean} options.trailing - 是否在结束时执行（默认 true）
 * @returns {Function} 节流后的函数
 * 
 * @example
 * // 滚动事件节流
 * const throttledScroll = throttle(() => {
 *   console.log('滚动位置:', window.scrollY)
 * }, 200)
 * 
 * // 在 scroll 事件中使用
 * onPageScroll(e) {
 *   throttledScroll()
 * }
 */
export function throttle(func, wait = 300, options = {}) {
	let timeout
	let previous = 0
	
	const { leading = true, trailing = true } = options
	
	return function(...args) {
		const context = this
		const now = Date.now()
		
		// 首次不执行
		if (!previous && !leading) {
			previous = now
		}
		
		// 计算剩余时间
		const remaining = wait - (now - previous)
		
		// 时间到了，执行函数
		if (remaining <= 0 || remaining > wait) {
			if (timeout) {
				clearTimeout(timeout)
				timeout = null
			}
			
			previous = now
			func.apply(context, args)
		} else if (!timeout && trailing) {
			// 设置定时器，在剩余时间后执行
			timeout = setTimeout(() => {
				previous = leading ? Date.now() : 0
				timeout = null
				func.apply(context, args)
			}, remaining)
		}
	}
}

/**
 * 创建一个只执行一次的函数
 * 
 * @param {Function} func - 需要执行的函数
 * @returns {Function} 只执行一次的函数
 * 
 * @example
 * const initOnce = once(() => {
 *   console.log('初始化')
 * })
 * 
 * initOnce() // 输出: 初始化
 * initOnce() // 不会执行
 */
export function once(func) {
	let called = false
	let result
	
	return function(...args) {
		if (!called) {
			called = true
			result = func.apply(this, args)
		}
		return result
	}
}

/**
 * 创建一个带有防抖功能的按钮点击处理器
 * 防止用户快速多次点击导致重复提交
 * 
 * @param {Function} handler - 点击处理函数
 * @param {number} wait - 防抖等待时间（毫秒）
 * @returns {Function} 防抖后的点击处理函数
 * 
 * @example
 * export default {
 *   methods: {
 *     handleSubmit: createDebouncedClick(async function() {
 *       // 提交表单
 *       await this.submitForm()
 *     }, 1000)
 *   }
 * }
 */
export function createDebouncedClick(handler, wait = 1000) {
	let isProcessing = false
	
	return async function(...args) {
		// 如果正在处理，直接返回
		if (isProcessing) {
			console.log('[Debounce] 操作进行中，请勿重复点击')
			uni.showToast({
				title: '操作进行中，请稍候',
				icon: 'none',
				duration: 1500
			})
			return
		}
		
		try {
			isProcessing = true
			await handler.apply(this, args)
		} finally {
			// 延迟重置状态，防止快速连续点击
			setTimeout(() => {
				isProcessing = false
			}, wait)
		}
	}
}

/**
 * 创建一个带有节流功能的滚动处理器
 * 
 * @param {Function} handler - 滚动处理函数
 * @param {number} wait - 节流等待时间（毫秒）
 * @returns {Function} 节流后的滚动处理函数
 * 
 * @example
 * export default {
 *   methods: {
 *     handleScroll: createThrottledScroll(function(e) {
 *       console.log('滚动位置:', e.scrollTop)
 *     }, 200)
 *   }
 * }
 */
export function createThrottledScroll(handler, wait = 200) {
	return throttle(handler, wait, { leading: true, trailing: true })
}

/**
 * 防抖装饰器（用于 Vue 组件方法）
 * 
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 装饰器函数
 * 
 * @example
 * export default {
 *   methods: {
 *     // 使用装饰器
 *     handleInput: debounceDecorator(500)(function(value) {
 *       console.log('输入:', value)
 *     })
 *   }
 * }
 */
export function debounceDecorator(wait = 300) {
	return function(target, name, descriptor) {
		const original = descriptor.value
		descriptor.value = debounce(original, wait)
		return descriptor
	}
}

/**
 * 节流装饰器（用于 Vue 组件方法）
 * 
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 装饰器函数
 * 
 * @example
 * export default {
 *   methods: {
 *     // 使用装饰器
 *     handleScroll: throttleDecorator(200)(function(e) {
 *       console.log('滚动:', e)
 *     })
 *   }
 * }
 */
export function throttleDecorator(wait = 300) {
	return function(target, name, descriptor) {
		const original = descriptor.value
		descriptor.value = throttle(original, wait)
		return descriptor
	}
}
