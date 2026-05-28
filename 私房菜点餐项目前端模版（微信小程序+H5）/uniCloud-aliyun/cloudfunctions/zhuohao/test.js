/**
 * 桌号管理云对象测试脚本
 * 
 * 使用方法：
 * 1. 在 HBuilderX 中右键点击 zhuohao 云对象
 * 2. 选择"运行-本地云函数"
 * 3. 复制下面的测试代码到测试窗口
 * 4. 点击运行
 */

// ==================== 测试用例 ====================

// 测试 1: 生成单个桌号小程序码
async function test1_generateSingleQRCode() {
	console.log('\n========== 测试 1: 生成单个桌号小程序码 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.generateQRCode('测试桌1')
		console.log('✅ 生成成功:', result)
		return result
	} catch (error) {
		console.error('❌ 生成失败:', error)
		return null
	}
}

// 测试 2: 测试桌号验证（空桌号）
async function test2_validateEmptyTableNumber() {
	console.log('\n========== 测试 2: 验证空桌号 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.generateQRCode('')
		console.log('结果:', result)
		if (!result.success && result.errCode === '1002') {
			console.log('✅ 空桌号验证通过')
		} else {
			console.log('❌ 空桌号验证失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 3: 测试桌号验证（特殊字符）
async function test3_validateSpecialCharacters() {
	console.log('\n========== 测试 3: 验证特殊字符 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.generateQRCode('桌号@#$%')
		console.log('结果:', result)
		if (!result.success && result.errCode === '1003') {
			console.log('✅ 特殊字符验证通过')
		} else {
			console.log('❌ 特殊字符验证失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 4: 测试桌号验证（超长桌号）
async function test4_validateLongTableNumber() {
	console.log('\n========== 测试 4: 验证超长桌号 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const longTableNumber = '这是一个非常非常非常非常非常非常长的桌号超过32个字符'
		const result = await zhuohao.generateQRCode(longTableNumber)
		console.log('结果:', result)
		if (!result.success && result.errCode === '1003') {
			console.log('✅ 超长桌号验证通过')
		} else {
			console.log('❌ 超长桌号验证失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 5: 测试重复桌号
async function test5_duplicateTableNumber() {
	console.log('\n========== 测试 5: 测试重复桌号 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		// 第一次生成
		const result1 = await zhuohao.generateQRCode('重复测试桌')
		console.log('第一次生成:', result1)
		
		// 第二次生成相同桌号
		const result2 = await zhuohao.generateQRCode('重复测试桌')
		console.log('第二次生成:', result2)
		
		if (!result2.success && result2.errCode === '1004') {
			console.log('✅ 重复桌号验证通过')
		} else {
			console.log('❌ 重复桌号验证失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 6: 查询桌号信息
async function test6_getTableInfo() {
	console.log('\n========== 测试 6: 查询桌号信息 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.getTableInfo('测试桌1')
		console.log('查询结果:', result)
		if (result.success) {
			console.log('✅ 查询成功')
		} else {
			console.log('❌ 查询失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 7: 获取桌号列表
async function test7_getTableList() {
	console.log('\n========== 测试 7: 获取桌号列表 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.getTableList(10, 1)
		console.log('列表结果:', result)
		if (result.success) {
			console.log('✅ 获取列表成功，共', result.data.total, '条记录')
		} else {
			console.log('❌ 获取列表失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 8: 删除桌号
async function test8_deleteTable() {
	console.log('\n========== 测试 8: 删除桌号 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	try {
		const result = await zhuohao.deleteTable('重复测试桌')
		console.log('删除结果:', result)
		if (result.success) {
			console.log('✅ 删除成功')
		} else {
			console.log('❌ 删除失败')
		}
	} catch (error) {
		console.error('❌ 测试失败:', error)
	}
}

// 测试 9: 批量生成桌号
async function test9_batchGenerate() {
	console.log('\n========== 测试 9: 批量生成桌号 ==========')
	const zhuohao = uniCloud.importObject('zhuohao')
	
	const results = []
	for (let i = 1; i <= 5; i++) {
		try {
			const result = await zhuohao.generateQRCode(`桌${i}`)
			results.push(result)
			console.log(`桌${i}:`, result.success ? '✅ 成功' : '❌ 失败')
		} catch (error) {
			console.error(`桌${i}: ❌ 错误`, error)
		}
	}
	
	const successCount = results.filter(r => r.success).length
	console.log(`\n批量生成完成: ${successCount}/5 成功`)
}

// ==================== 运行所有测试 ====================

async function runAllTests() {
	console.log('🚀 开始运行测试...\n')
	
	await test1_generateSingleQRCode()
	await test2_validateEmptyTableNumber()
	await test3_validateSpecialCharacters()
	await test4_validateLongTableNumber()
	await test5_duplicateTableNumber()
	await test6_getTableInfo()
	await test7_getTableList()
	await test8_deleteTable()
	await test9_batchGenerate()
	
	console.log('\n✅ 所有测试完成！')
}

// 导出测试函数
module.exports = {
	runAllTests,
	test1_generateSingleQRCode,
	test2_validateEmptyTableNumber,
	test3_validateSpecialCharacters,
	test4_validateLongTableNumber,
	test5_duplicateTableNumber,
	test6_getTableInfo,
	test7_getTableList,
	test8_deleteTable,
	test9_batchGenerate
}

// 如果直接运行此文件，执行所有测试
// runAllTests()
