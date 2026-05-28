/**
 * 状态管理工具
 * 提供全局状态管理功能
 */

import { TableStorage, CartStorage, UserStorage } from './localStorage.js'

/**
 * 全局状态对象
 */
const globalState = {
	// 桌号信息
	table: {
		tableNumber: null,
		dinerCount: null,
		scanTime: null,
		isConfirmed: false
	},
	
	// 购物车
	cart: {
		items: [],
		totalCount: 0,
		totalPrice: 0
	},
	
	// 用户信息
	user: {
		isLoggedIn: false,
		userInfo: null
	},
	
	// 应用状态
	app: {
		isLoading: false,
		networkStatus: 'online'
	}
}

/**
 * 状态监听器
 */
const listeners = {
	table: [],
	cart: [],
	user: [],
	app: []
}

/**
 * 初始化状态
 * 从本地存储恢复状态
 */
export function initState() {
	try {
		// 恢复桌号信息
		const tableInfo = TableStorage.get()
		if (tableInfo.tableNumber) {
			globalState.table = {
				tableNumber: tableInfo.tableNumber,
				dinerCount: tableInfo.dinerCount,
				scanTime: tableInfo.scanTime,
				isConfirmed: true
			}
		}
		
		// 恢复购物车
		const cartItems = CartStorage.get()
		updateCartState(cartItems)
		
		// 恢复用户信息
		const userInfo = UserStorage.get()
		if (userInfo) {
			globalState.user = {
				isLoggedIn: true,
				userInfo
			}
		}
		
		console.log('[StateManager] 状态初始化完成')
	} catch (error) {
		console.error('[StateManager] 状态初始化失败:', error)
	}
}

/**
 * 更新购物车状态
 * @param {array} items - 购物车商品列表
 */
function updateCartState(items) {
	globalState.cart.items = items
	globalState.cart.totalCount = items.reduce((sum, item) => sum + item.count, 0)
	globalState.cart.totalPrice = items.reduce((sum, item) => sum + item.price * item.count, 0)
}

/**
 * 获取状态
 * @param {string} key - 状态键名
 * @returns {any} 状态值
 */
export function getState(key) {
	if (key) {
		return globalState[key]
	}
	return globalState
}

/**
 * 设置桌号状态
 * @param {string} tableNumber - 桌号
 * @param {number} dinerCount - 就餐人数
 * @param {boolean} save - 是否保存到本地存储
 */
export function setTableState(tableNumber, dinerCount, save = true) {
	globalState.table = {
		tableNumber,
		dinerCount,
		scanTime: Date.now(),
		isConfirmed: true
	}
	
	// 保存到本地存储
	if (save) {
		TableStorage.save(tableNumber, dinerCount)
	}
	
	// 通知监听器
	notifyListeners('table', globalState.table)
	
	console.log('[StateManager] 桌号状态已更新:', globalState.table)
}

/**
 * 清除桌号状态
 * @param {boolean} save - 是否同步到本地存储
 */
export function clearTableState(save = true) {
	globalState.table = {
		tableNumber: null,
		dinerCount: null,
		scanTime: null,
		isConfirmed: false
	}
	
	// 清除本地存储
	if (save) {
		TableStorage.clear()
	}
	
	// 通知监听器
	notifyListeners('table', globalState.table)
	
	console.log('[StateManager] 桌号状态已清除')
}

/**
 * 获取桌号状态
 * @returns {object} 桌号信息
 */
export function getTableState() {
	return globalState.table
}

/**
 * 检查是否已确认桌号
 * @returns {boolean} 是否已确认
 */
export function isTableConfirmed() {
	return globalState.table.isConfirmed && globalState.table.tableNumber !== null
}

/**
 * 添加商品到购物车
 * @param {object} item - 商品信息
 */
export function addToCart(item) {
	// 更新本地存储
	CartStorage.addItem(item)
	
	// 更新状态
	const cartItems = CartStorage.get()
	updateCartState(cartItems)
	
	// 通知监听器
	notifyListeners('cart', globalState.cart)
	
	console.log('[StateManager] 商品已添加到购物车:', item)
}

/**
 * 从购物车移除商品
 * @param {string} itemId - 商品ID
 */
export function removeFromCart(itemId) {
	// 更新本地存储
	CartStorage.removeItem(itemId)
	
	// 更新状态
	const cartItems = CartStorage.get()
	updateCartState(cartItems)
	
	// 通知监听器
	notifyListeners('cart', globalState.cart)
	
	console.log('[StateManager] 商品已从购物车移除:', itemId)
}

/**
 * 更新购物车商品数量
 * @param {string} itemId - 商品ID
 * @param {number} count - 数量
 */
export function updateCartItemCount(itemId, count) {
	// 更新本地存储
	CartStorage.updateItemCount(itemId, count)
	
	// 更新状态
	const cartItems = CartStorage.get()
	updateCartState(cartItems)
	
	// 通知监听器
	notifyListeners('cart', globalState.cart)
	
	console.log('[StateManager] 购物车商品数量已更新:', itemId, count)
}

/**
 * 清空购物车
 */
export function clearCart() {
	// 清除本地存储
	CartStorage.clear()
	
	// 更新状态
	updateCartState([])
	
	// 通知监听器
	notifyListeners('cart', globalState.cart)
	
	console.log('[StateManager] 购物车已清空')
}

/**
 * 获取购物车状态
 * @returns {object} 购物车信息
 */
export function getCartState() {
	return globalState.cart
}

/**
 * 设置用户状态
 * @param {object} userInfo - 用户信息
 * @param {boolean} save - 是否保存到本地存储
 */
export function setUserState(userInfo, save = true) {
	globalState.user = {
		isLoggedIn: true,
		userInfo
	}
	
	// 保存到本地存储
	if (save) {
		UserStorage.save(userInfo)
	}
	
	// 通知监听器
	notifyListeners('user', globalState.user)
	
	console.log('[StateManager] 用户状态已更新')
}

/**
 * 清除用户状态（退出登录）
 * @param {boolean} save - 是否同步到本地存储
 */
export function clearUserState(save = true) {
	globalState.user = {
		isLoggedIn: false,
		userInfo: null
	}
	
	// 清除本地存储
	if (save) {
		UserStorage.clear()
	}
	
	// 通知监听器
	notifyListeners('user', globalState.user)
	
	console.log('[StateManager] 用户状态已清除')
}

/**
 * 获取用户状态
 * @returns {object} 用户信息
 */
export function getUserState() {
	return globalState.user
}

/**
 * 检查是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
	return globalState.user.isLoggedIn
}

/**
 * 设置加载状态
 * @param {boolean} isLoading - 是否加载中
 */
export function setLoadingState(isLoading) {
	globalState.app.isLoading = isLoading
	notifyListeners('app', globalState.app)
}

/**
 * 设置网络状态
 * @param {string} status - 网络状态 'online' | 'offline'
 */
export function setNetworkStatus(status) {
	globalState.app.networkStatus = status
	notifyListeners('app', globalState.app)
}

/**
 * 获取应用状态
 * @returns {object} 应用状态
 */
export function getAppState() {
	return globalState.app
}

/**
 * 添加状态监听器
 * @param {string} key - 状态键名
 * @param {function} callback - 回调函数
 * @returns {function} 取消监听函数
 */
export function addListener(key, callback) {
	if (!listeners[key]) {
		listeners[key] = []
	}
	
	listeners[key].push(callback)
	
	// 返回取消监听函数
	return () => {
		const index = listeners[key].indexOf(callback)
		if (index > -1) {
			listeners[key].splice(index, 1)
		}
	}
}

/**
 * 移除状态监听器
 * @param {string} key - 状态键名
 * @param {function} callback - 回调函数
 */
export function removeListener(key, callback) {
	if (listeners[key]) {
		const index = listeners[key].indexOf(callback)
		if (index > -1) {
			listeners[key].splice(index, 1)
		}
	}
}

/**
 * 通知监听器
 * @param {string} key - 状态键名
 * @param {any} value - 状态值
 */
function notifyListeners(key, value) {
	if (listeners[key]) {
		listeners[key].forEach(callback => {
			try {
				callback(value)
			} catch (error) {
				console.error('[StateManager] 监听器执行失败:', error)
			}
		})
	}
}

/**
 * 重置所有状态
 */
export function resetAllState() {
	clearTableState()
	clearCart()
	clearUserState()
	
	globalState.app = {
		isLoading: false,
		networkStatus: 'online'
	}
	
	console.log('[StateManager] 所有状态已重置')
}

// 导出默认对象
export default {
	initState,
	getState,
	setTableState,
	clearTableState,
	getTableState,
	isTableConfirmed,
	addToCart,
	removeFromCart,
	updateCartItemCount,
	clearCart,
	getCartState,
	setUserState,
	clearUserState,
	getUserState,
	isLoggedIn,
	setLoadingState,
	setNetworkStatus,
	getAppState,
	addListener,
	removeListener,
	resetAllState
}
