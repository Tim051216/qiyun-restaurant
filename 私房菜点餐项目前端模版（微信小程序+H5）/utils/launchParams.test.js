/**
 * 启动参数处理工具测试
 * 这是一个简单的测试文件，用于验证启动参数处理逻辑
 */

import { parseSceneParam, detectEnvironment, handleLaunchParams } from './launchParams.js'

// 测试用例
const testCases = {
	// 测试 parseSceneParam
	parseSceneParam: [
		{
			name: '标准格式 - table=A01',
			input: 'table=A01',
			expected: { table: 'A01' }
		},
		{
			name: '多参数格式 - table=A01&dinerCount=4',
			input: 'table=A01&dinerCount=4',
			expected: { table: 'A01', dinerCount: '4' }
		},
		{
			name: '直接桌号 - A01',
			input: 'A01',
			expected: { table: 'A01' }
		},
		{
			name: 'URL 编码格式',
			input: encodeURIComponent('table=A01'),
			expected: { table: 'A01' }
		},
		{
			name: '空参数',
			input: '',
			expected: {}
		},
		{
			name: 'null 参数',
			input: null,
			expected: {}
		}
	]
}

// 运行测试
function runTests() {
	console.log('=== 启动参数处理工具测试 ===\n')
	
	let passCount = 0
	let failCount = 0
	
	// 测试 parseSceneParam
	console.log('测试 parseSceneParam:')
	testCases.parseSceneParam.forEach(testCase => {
		try {
			const result = parseSceneParam(testCase.input)
			const passed = JSON.stringify(result) === JSON.stringify(testCase.expected)
			
			if (passed) {
				console.log(`✓ ${testCase.name}`)
				passCount++
			} else {
				console.log(`✗ ${testCase.name}`)
				console.log(`  期望: ${JSON.stringify(testCase.expected)}`)
				console.log(`  实际: ${JSON.stringify(result)}`)
				failCount++
			}
		} catch (error) {
			console.log(`✗ ${testCase.name} - 异常: ${error.message}`)
			failCount++
		}
	})
	
	console.log(`\n测试结果: ${passCount} 通过, ${failCount} 失败`)
	
	return { passCount, failCount }
}

// 导出测试函数
export { runTests }

// 如果直接运行此文件，执行测试
if (typeof module !== 'undefined' && module.exports) {
	runTests()
}
