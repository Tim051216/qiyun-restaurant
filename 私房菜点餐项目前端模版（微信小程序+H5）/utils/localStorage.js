/**
 * 本地存储工具
 * 提供统一的本地存储接口，支持跨平台
 */

import { StorageAdapter } from './envAdapter.js'

/**
 * 存储键名常量
 */
export const STORAGE_KEYS = {
	TABLE_NUMBER: 'tableNumber',
	DINER_COUNT: 'dinerCount',
	SCAN_TIME: 'scanTime',
	USER_INFO: 'userInfo',
	CART: 'cart',
	ORDER_HISTORY: 'orderHistory'
}

/**
 * 保存到本地存储
 * @param {string} key - 键名
 * @param {any} value - 值
 * @returns {boolean} 是否成功
 */
export function saveToLocalStorage(key, value) {
	try {
		const success = StorageAdapter.set(key, value)
		if (success) {
			console.log(`[LocalStorage] 保存成功: ${key}`)
		} else {
			console.error(`[LocalStorage] 保存失败: ${key}`)
		}
		return success
	} catch (error) {
		console.error(`[LocalStorage] 保存异常: ${key}`, error)
		return false
	}
}

/**
 * 从本地存储获取
 * @param {string} key - 键名
 * @param {any} defaultValue - 默认值
 * @returns {any} 值
 */
export function getFromLocalStorage(key, defaultValue = null) {
	try {
		const value = StorageAdapter.get(key)
		if (value !== null && value !== undefined) {
			console.log(`[LocalStorage] 读取成功: ${key}`)
			return value
		}
		console.log(`[LocalStorage] 键不存在，返回默认值: ${key}`)
		return defaultValue
	} catch (error) {
		console.error(`[LocalStorage] 读取异常: ${key}`, error)
		return defaultValue
	}
}

/**
 * 从本地存储删除
 * @param {string} key - 键名
 * @returns {boolean} 是否成功
 */
export function removeFromLocalStorage(key) {
	try {
		const success = StorageAdapter.remove(key)
		if (success) {
			console.log(`[LocalStorage] 删除成功: ${key}`)
		} else {
			console.error(`[LocalStorage] 删除失败: ${key}`)
		}
		return success
	} catch (error) {
		console.error(`[LocalStorage] 删除异常: ${key}`, error)
		return false
	}
}

/**
 * 清空本地存储
 * @returns {boolean} 是否成功
 */
export function clearLocalStorage() {
	try {
		const success = StorageAdapter.clear()
		if (success) {
			console.log('[LocalStorage] 清空成功')
		} else {
			console.error('[LocalStorage] 清空失败')
		}
		return success
	} catch (error) {
		console.error('[LocalStorage] 清空异常', error)
		return false
	}
}

/**
 * 检查键是否存在
 * @param {string} key - 键名
 * @returns {boolean} 是否存在
 */
export function hasKey(key) {
	try {
		const value = StorageAdapter.get(key)
		return value !== null && value !== undefined
	} catch (error) {
		console.error(`[LocalStorage] 检查键异常: ${key}`, error)
		return false
	}
}

/**
 * 批量保存
 * @param {object} data - 键值对对象
 * @returns {boolean} 是否全部成功
 */
export function batchSave(data) {
	try {
		let allSuccess = true
		Object.keys(data).forEach(key => {
			const success = saveToLocalStorage(key, data[key])
			if (!success) {
				allSuccess = false
			}
		})
		return allSuccess
	} catch (error) {
		console.error('[LocalStorage] 批量保存异常', error)
		return false
	}
}

/**
 * 批量获取
 * @param {array} keys - 键名数组
 * @returns {object} 键值对对象
 */
export function batchGet(keys) {
	try {
		const result = {}
		keys.forEach(key => {
			result[key] = getFromLocalStorage(key)
		})
		return result
	} catch (error) {
		console.error('[LocalStorage] 批量获取异常', error)
		return {}
	}
}

/**
 * 批量删除
 * @param {array} keys - 键名数组
 * @returns {boolean} 是否全部成功
 */
export function batchRemove(keys) {
	try {
		let allSuccess = true
		keys.forEach(key => {
			const success = removeFromLocalStorage(key)
			if (!success) {
				allSuccess = false
			}
		})
		return allSuccess
	} catch (error) {
		console.error('[LocalStorage] 批量删除异常', error)
		return false
	}
}

/**
 * 获取所有键名
 * @returns {array} 键名数组
 */
export function getAllKeys() {
	try {
		// 注意：这个功能在不同环境下实现可能不同
		// 小程序环境可以使用 uni.getStorageInfoSync()
		// H5 环境可以使用 Object.keys(localStorage)
		
		// #ifdef MP-WEIXIN
		const info = uni.getStorageInfoSync()
		return info.keys || []
		// #endif
		
		// #ifdef H5
		if (typeof localStorage !== 'undefined') {
			return Object.keys(localStorage)
		}
		return []
		// #endif
		
		return []
	} catch (error) {
		console.error('[LocalStorage] 获取所有键名异常', error)
		return []
	}
}

/**
 * 获取存储信息
 * @returns {object} 存储信息
 */
export function getStorageInfo() {
	try {
		// #ifdef MP-WEIXIN
		const info = uni.getStorageInfoSync()
		return {
			keys: info.keys || [],
			currentSize: info.currentSize || 0,
			limitSize: info.limitSize || 0
		}
		// #endif
		
		// #ifdef H5
		if (typeof localStorage !== 'undefined') {
			const keys = Object.keys(localStorage)
			return {
				keys,
				currentSize: 0, // H5 环境无法准确获取
				limitSize: 0
			}
		}
		return {
			keys: [],
			currentSize: 0,
			limitSize: 0
		}
		// #endif
		
		return {
			keys: [],
			currentSize: 0,
			limitSize: 0
		}
	} catch (error) {
		console.error('[LocalStorage] 获取存储信息异常', error)
		return {
			keys: [],
			currentSize: 0,
			limitSize: 0
		}
	}
}

/**
 * 桌号相关存储操作
 */
export const TableStorage = {
	/**
	 * 保存桌号信息
	 * @param {string} tableNumber - 桌号
	 * @param {number} dinerCount - 就餐人数
	 * @returns {boolean} 是否成功
	 */
	save(tableNumber, dinerCount) {
		return batchSave({
			[STORAGE_KEYS.TABLE_NUMBER]: tableNumber,
			[STORAGE_KEYS.DINER_COUNT]: dinerCount,
			[STORAGE_KEYS.SCAN_TIME]: Date.now()
		})
	},
	
	/**
	 * 获取桌号信息
	 * @returns {object} 桌号信息
	 */
	get() {
		const data = batchGet([
			STORAGE_KEYS.TABLE_NUMBER,
			STORAGE_KEYS.DINER_COUNT,
			STORAGE_KEYS.SCAN_TIME
		])
		
		return {
			tableNumber: data[STORAGE_KEYS.TABLE_NUMBER],
			dinerCount: data[STORAGE_KEYS.DINER_COUNT],
			scanTime: data[STORAGE_KEYS.SCAN_TIME]
		}
	},
	
	/**
	 * 清除桌号信息
	 * @returns {boolean} 是否成功
	 */
	clear() {
		return batchRemove([
			STORAGE_KEYS.TABLE_NUMBER,
			STORAGE_KEYS.DINER_COUNT,
			STORAGE_KEYS.SCAN_TIME
		])
	},
	
	/**
	 * 检查是否有桌号信息
	 * @returns {boolean} 是否存在
	 */
	has() {
		return hasKey(STORAGE_KEYS.TABLE_NUMBER)
	}
}

/**
 * 购物车相关存储操作
 */
export const CartStorage = {
	/**
	 * 保存购物车
	 * @param {array} cart - 购物车数据
	 * @returns {boolean} 是否成功
	 */
	save(cart) {
		return saveToLocalStorage(STORAGE_KEYS.CART, cart)
	},
	
	/**
	 * 获取购物车
	 * @returns {array} 购物车数据
	 */
	get() {
		return getFromLocalStorage(STORAGE_KEYS.CART, [])
	},
	
	/**
	 * 清空购物车
	 * @returns {boolean} 是否成功
	 */
	clear() {
		return removeFromLocalStorage(STORAGE_KEYS.CART)
	},
	
	/**
	 * 添加商品到购物车
	 * @param {object} item - 商品信息
	 * @returns {boolean} 是否成功
	 */
	addItem(item) {
		const cart = this.get()
		const existingIndex = cart.findIndex(i => i.id === item.id)
		
		if (existingIndex >= 0) {
			// 商品已存在，增加数量
			cart[existingIndex].count += item.count || 1
		} else {
			// 新商品，添加到购物车
			cart.push({
				...item,
				count: item.count || 1
			})
		}
		
		return this.save(cart)
	},
	
	/**
	 * 从购物车移除商品
	 * @param {string} itemId - 商品ID
	 * @returns {boolean} 是否成功
	 */
	removeItem(itemId) {
		const cart = this.get()
		const filteredCart = cart.filter(item => item.id !== itemId)
		return this.save(filteredCart)
	},
	
	/**
	 * 更新商品数量
	 * @param {string} itemId - 商品ID
	 * @param {number} count - 数量
	 * @returns {boolean} 是否成功
	 */
	updateItemCount(itemId, count) {
		const cart = this.get()
		const item = cart.find(i => i.id === itemId)
		
		if (item) {
			if (count <= 0) {
				// 数量为0，移除商品
				return this.removeItem(itemId)
			} else {
				item.count = count
				return this.save(cart)
			}
		}
		
		return false
	}
}

/**
 * 用户信息相关存储操作
 */
export const UserStorage = {
	/**
	 * 保存用户信息
	 * @param {object} userInfo - 用户信息
	 * @returns {boolean} 是否成功
	 */
	save(userInfo) {
		return saveToLocalStorage(STORAGE_KEYS.USER_INFO, userInfo)
	},
	
	/**
	 * 获取用户信息
	 * @returns {object} 用户信息
	 */
	get() {
		return getFromLocalStorage(STORAGE_KEYS.USER_INFO, null)
	},
	
	/**
	 * 清除用户信息
	 * @returns {boolean} 是否成功
	 */
	clear() {
		return removeFromLocalStorage(STORAGE_KEYS.USER_INFO)
	},
	
	/**
	 * 检查是否已登录
	 * @returns {boolean} 是否已登录
	 */
	isLoggedIn() {
		const userInfo = this.get()
		return userInfo !== null && userInfo.id
	}
}

// 导出默认对象
export default {
	STORAGE_KEYS,
	saveToLocalStorage,
	getFromLocalStorage,
	removeFromLocalStorage,
	clearLocalStorage,
	hasKey,
	batchSave,
	batchGet,
	batchRemove,
	getAllKeys,
	getStorageInfo,
	TableStorage,
	CartStorage,
	UserStorage
}
