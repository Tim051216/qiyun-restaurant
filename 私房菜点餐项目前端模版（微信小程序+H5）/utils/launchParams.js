/**
 * 启动参数处理工具
 * 支持微信小程序和 H5 两种环境的参数获取
 */

import { detectEnvironment } from './envAdapter.js'
import { TableStorage } from './localStorage.js'

/**
 * 解析 scene 参数
 * @param {string} scene - 场景值参数
 * @returns {object} 解析后的参数对象
 */
export function parseSceneParam(scene) {
	if (!scene) {
		return {}
	}
	
	try {
		// 尝试 URL 解码
		let decodedScene = decodeURIComponent(scene)
		
		// 支持格式: table=桌号 或 直接是桌号
		const params = {}
		
		if (decodedScene.includes('=')) {
			// 格式: table=A01 或 table=A01&other=value
			const pairs = decodedScene.split('&')
			pairs.forEach(pair => {
				const [key, value] = pair.split('=')
				if (key && value) {
					params[key] = value
				}
			})
		} else {
			// 直接是桌号
			params.table = decodedScene
		}
		
		console.log('[LaunchParams] Scene 参数解析成功:', params)
		return params
	} catch (error) {
		console.error('[LaunchParams] Scene 参数解析失败:', error)
		return {}
	}
}

/**
 * 从 URL 中获取查询参数 (H5 环境)
 * @returns {object} 查询参数对象
 */
function getH5QueryParams() {
	const params = {}
	
	try {
		// 获取 URL 查询字符串
		const search = window.location.search
		if (!search) {
			return params
		}
		
		// 解析查询参数
		const urlParams = new URLSearchParams(search)
		urlParams.forEach((value, key) => {
			params[key] = value
		})
		
		console.log('[LaunchParams] H5 URL 参数:', params)
	} catch (error) {
		console.error('[LaunchParams] H5 参数解析失败:', error)
	}
	
	return params
}

/**
 * 从小程序启动参数中获取参数
 * @param {object} options - onLoad 的 options 参数
 * @returns {object} 启动参数对象
 */
function getMiniProgramParams(options) {
	const params = {}
	
	try {
		// 1. 从 options 中获取参数
		if (options) {
			Object.keys(options).forEach(key => {
				params[key] = options[key]
			})
		}
		
		// 2. 处理 scene 参数 (小程序码场景值)
		if (options && options.scene) {
			const sceneParams = parseSceneParam(options.scene)
			Object.assign(params, sceneParams)
		}
		
		// 3. 尝试使用 getLaunchOptionsSync (兼容方案)
		if (uni.getLaunchOptionsSync) {
			const launchOptions = uni.getLaunchOptionsSync()
			if (launchOptions && launchOptions.query) {
				Object.assign(params, launchOptions.query)
			}
			
			// 处理 referrerInfo 中的场景值
			if (launchOptions && launchOptions.referrerInfo && launchOptions.referrerInfo.extraData) {
				Object.assign(params, launchOptions.referrerInfo.extraData)
			}
		}
		
		console.log('[LaunchParams] 小程序启动参数:', params)
	} catch (error) {
		console.error('[LaunchParams] 小程序参数解析失败:', error)
	}
	
	return params
}

/**
 * 统一处理启动参数
 * 支持小程序和 H5 两种环境
 * @param {object} options - onLoad 的 options 参数 (小程序环境必传)
 * @returns {object} 启动参数对象
 */
export function handleLaunchParams(options = {}) {
	const env = detectEnvironment()
	let params = {}
	
	console.log('[LaunchParams] 当前环境:', env)
	
	if (env === 'mp-weixin') {
		// 小程序环境
		params = getMiniProgramParams(options)
	} else if (env === 'h5') {
		// H5 环境
		params = getH5QueryParams()
		
		// H5 也可能通过 URL 传递 scene 参数
		if (params.scene) {
			const sceneParams = parseSceneParam(params.scene)
			Object.assign(params, sceneParams)
		}
	} else {
		console.warn('[LaunchParams] 未知环境')
	}
	
	return params
}

/**
 * 获取桌号信息
 * 从启动参数或本地存储中获取
 * @param {object} options - onLoad 的 options 参数
 * @returns {object} { tableNumber, dinerCount, scanTime }
 */
export function getTableInfo(options = {}) {
	// 1. 尝试从启动参数获取
	const params = handleLaunchParams(options)
	
	if (params.table || params.tableNumber) {
		return {
			tableNumber: params.table || params.tableNumber,
			dinerCount: params.dinerCount ? parseInt(params.dinerCount) : null,
			scanTime: Date.now(),
			fromScan: true
		}
	}
	
	// 2. 从本地存储获取
	try {
		const tableInfo = TableStorage.get()
		
		if (tableInfo.tableNumber) {
			return {
				tableNumber: tableInfo.tableNumber,
				dinerCount: tableInfo.dinerCount ? parseInt(tableInfo.dinerCount) : null,
				scanTime: tableInfo.scanTime || null,
				fromScan: false
			}
		}
	} catch (error) {
		console.error('[LaunchParams] 读取本地存储失败:', error)
	}
	
	return {
		tableNumber: null,
		dinerCount: null,
		scanTime: null,
		fromScan: false
	}
}

/**
 * 保存桌号信息到本地存储
 * @param {string} tableNumber - 桌号
 * @param {number} dinerCount - 就餐人数
 */
export function saveTableInfo(tableNumber, dinerCount) {
	try {
		const success = TableStorage.save(tableNumber, dinerCount)
		if (success) {
			console.log('[LaunchParams] 桌号信息已保存:', { tableNumber, dinerCount })
		}
		return success
	} catch (error) {
		console.error('[LaunchParams] 保存桌号信息失败:', error)
		return false
	}
}

/**
 * 清除桌号信息
 */
export function clearTableInfo() {
	try {
		const success = TableStorage.clear()
		if (success) {
			console.log('[LaunchParams] 桌号信息已清除')
		}
		return success
	} catch (error) {
		console.error('[LaunchParams] 清除桌号信息失败:', error)
		return false
	}
}

// 重新导出 detectEnvironment
export { detectEnvironment }
