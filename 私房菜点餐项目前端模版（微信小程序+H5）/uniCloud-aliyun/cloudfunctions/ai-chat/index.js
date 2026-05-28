'use strict';

// 阿里云通义千问云函数
// 使用 DashScope API Key 安全调用

// 配置信息
const API_KEY = 'sk-7daca2a765c440c39ada2566908488eb';
const API_URL = 'https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation';

// 频率限制配置
const requestCache = {};
const RATE_LIMIT = 10; // 每分钟最多 10 次请求
const MAX_HISTORY = 5; // 最多保留 5 条历史记录

exports.main = async (event, context) => {
	console.log('云函数开始执行，接收参数:', event);
	
	// 频率限制检查
	const userId = context.OPENID || 'anonymous';
	const now = Date.now();
	
	if (requestCache[userId]) {
		const { count, timestamp } = requestCache[userId];
		if (now - timestamp < 60000) { // 1 分钟内
			if (count >= RATE_LIMIT) {
				console.log('请求频率超限:', userId);
				return {
					code: 429,
					success: false,
					message: '请求过于频繁，请稍后再试'
				};
			}
			requestCache[userId].count++;
		} else {
			requestCache[userId] = { count: 1, timestamp: now };
		}
	} else {
		requestCache[userId] = { count: 1, timestamp: now };
	}
	
	let { message, history = [] } = event;
	
	// 限制历史记录数量
	if (history.length > MAX_HISTORY) {
		history = history.slice(-MAX_HISTORY);
		console.log('历史记录已截断至', MAX_HISTORY, '条');
	}
	
	// 验证参数
	if (!message) {
		console.log('参数验证失败：消息为空');
		return {
			code: 400,
			success: false,
			message: '消息不能为空'
		};
	}
	
	try {
		console.log('开始构建请求...');
		
		// 构建消息历史
		const messages = [
			{
				role: 'system',
				content: '你是七云菜馆的智能点餐助手，你可以帮助用户推荐菜品、解答问题、协助点餐。请用友好、专业的语气回答用户问题。'
			},
			...history,
			{
				role: 'user',
				content: message
			}
		];
		
		console.log('消息列表:', JSON.stringify(messages));
		
		// 构建请求体
		const requestBody = {
			model: 'qwen-turbo',
			input: {
				messages: messages
			},
			parameters: {
				result_format: 'message',
				max_tokens: 500,
				temperature: 0.7
			}
		};
		
		console.log('开始调用通义千问 API...');
		
		// 发起请求
		const response = await uniCloud.httpclient.request(API_URL, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				'Authorization': `Bearer ${API_KEY}`
			},
			data: requestBody,
			dataType: 'json',
			timeout: 30000 // 30秒超时
		});
		
		console.log('API 响应状态:', response.status);
		console.log('API 响应数据:', JSON.stringify(response.data));
		
		// 处理响应
		if (response.status === 200 && response.data.output) {
			const reply = response.data.output.choices[0].message.content;
			console.log('AI 回复成功:', reply);
			return {
				code: 200,
				success: true,
				message: reply,
				usage: response.data.usage
			};
		} else {
			console.error('AI 调用失败，响应数据:', response.data);
			return {
				code: 500,
				success: false,
				message: '抱歉，AI 助手暂时无法回复',
				error: response.data
			};
		}
	} catch (error) {
		console.error('云函数执行失败，错误详情:', error);
		console.error('错误堆栈:', error.stack);
		return {
			code: 500,
			success: false,
			message: '服务器错误，请稍后重试',
			error: error.message,
			stack: error.stack
		};
	}
};
