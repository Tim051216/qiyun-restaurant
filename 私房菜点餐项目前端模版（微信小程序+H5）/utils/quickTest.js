/**
 * 快速测试脚本
 * 用于验证工具函数的基本功能
 */

import { parseSceneParam, handleLaunchParams } from './launchParams.js'
import { detectEnvironment, isFeatureSupported } from './envAdapter.js'
import { saveToLocalStorage, getFromLocalStorage, TableStorage, CartStorage } from './localStorage.js'
import { initState, setTableState, getTableState, addToCart, getCartState } from './stateManager.js'

/**
 * 测试结果
 */
const testResults = {
	passed: 0,
	failed: 0,
	tests: []
}

/**
 * 断言函数
 */
function assert(condition, message) {
	if (condition) {
		testResults.passed++
		testResults.tests.push({ status: 'PASS', message })
		console.log(`✓ ${message}`)
	} else {
		testResults.failed++
		testResults.tests.push({ status: 'FAIL', message })
		console.error(`✗ ${message}`)
	}
}

/**
 * 测试 parseSceneParam
 */
function testParseSceneParam() {
	console.log('\n=== 测试 parseSceneParam ===')
	
	// 测试标准格式
	const result1 = parseSceneParam('table=A01')
	assert(result1.table === 'A01', '解析标准格式: table=A01')
	
	// 测试多参数格式
	const result2 = parseSceneParam('table=A01&dinerCount=4')
	assert(result2.table === 'A01' && result2.dinerCount === '4', '解析多参数格式')
	
	// 测试直接桌号
	const result3 = parseSceneParam('A01')
	assert(result3.table === 'A01', '解析直接桌号格式')
	
	// 测试空参数
	const result4 = parseSceneParam('')
	assert(Object.keys(result4).length === 0, '解析空参数')
}

/**
 * 测试环境检测
 */
function testEnvironment() {
	console.log('\n=== 测试环境检测 ===')
	
	const env = detectEnvironment()
	assert(env === 'mp-weixin' || env === 'h5' || env === 'app', `检测到环境: ${env}`)
	
	// 测试功能支持检测
	const scanSupported = isFeatureSupported('scan')
	console.log(`扫码功能支持: ${scanSupported}`)
}

/**
 * 测试本地存储
 */
function testLocalStorage() {
	console.log('\n=== 测试本地存储 ===')
	
	// 测试基础存储
	const testKey = 'test_key'
	const testValue = { name: 'test', value: 123 }
	
	const saved = saveToLocalStorage(testKey, testValue)
	assert(saved, '保存数据到本地存储')
	
	const retrieved = getFromLocalStorage(testKey)
	assert(JSON.stringify(retrieved) === JSON.stringify(testValue), '从本地存储读取数据')
	
	// 测试 TableStorage
	const tableSaved = TableStorage.save('A01', 4)
	assert(tableSaved, '保存桌号信息')
	
	const tableInfo = TableStorage.get()
	assert(tableInfo.tableNumber === 'A01' && tableInfo.dinerCount === 4, '读取桌号信息')
	
	const hasTable = TableStorage.has()
	assert(hasTable, '检查桌号是否存在')
	
	// 测试 CartStorage
	const item = { id: 1, name: '宫保鸡丁', price: 38, count: 1 }
	const cartSaved = CartStorage.addItem(item)
	assert(cartSaved, '添加商品到购物车')
	
	const cart = CartStorage.get()
	assert(cart.length > 0, '读取购物车数据')
	
	// 清理测试数据
	TableStorage.clear()
	CartStorage.clear()
}

/**
 * 测试状态管理
 */
function testStateManager() {
	console.log('\n=== 测试状态管理 ===')
	
	// 初始化状态
	initState()
	assert(true, '初始化状态管理')
	
	// 测试桌号状态
	setTableState('B02', 6, false)
	const tableState = getTableState()
	assert(tableState.tableNumber === 'B02' && tableState.dinerCount === 6, '设置和获取桌号状态')
	
	// 测试购物车状态
	const item = { id: 2, name: '麻婆豆腐', price: 28, count: 1 }
	addToCart(item)
	const cartState = getCartState()
	assert(cartState.totalCount > 0, '添加商品到购物车状态')
	
	console.log('购物车状态:', cartState)
}

/**
 * 运行所有测试
 */
export function runAllTests() {
	console.log('========================================')
	console.log('开始运行快速测试')
	console.log('========================================')
	
	try {
		testParseSceneParam()
		testEnvironment()
		testLocalStorage()
		testStateManager()
		
		console.log('\n========================================')
		console.log('测试完成')
		console.log(`通过: ${testResults.passed}`)
		console.log(`失败: ${testResults.failed}`)
		console.log(`总计: ${testResults.passed + testResults.failed}`)
		console.log('========================================')
		
		return testResults
	} catch (error) {
		console.error('测试执行出错:', error)
		return {
			error: error.message,
			...testResults
		}
	}
}

/**
 * 导出测试函数
 */
export default {
	runAllTests,
	testParseSceneParam,
	testEnvironment,
	testLocalStorage,
	testStateManager
}
