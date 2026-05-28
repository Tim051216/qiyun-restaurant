import request from '@/utils/request.js'

/**
 * AI 助手 API
 * 通义千问集成 - 调用自己的后端服务（安全）
 */

/**
 * 调用通义千问 AI（通过后端API）
 * @param {String} message 用户消息
 * @param {Array} history 历史对话记录
 * @returns {Promise}
 */
export function chatWithAI(message, history = []) {
	console.log('[AI] 开始调用后端API，消息:', message);
	
	return request({
		url: '/ai/chat',
		method: 'POST',
		data: {
			message,
			history
		}
	}).then(res => {
		console.log('[AI] 后端返回:', res);
		
		if (res.code === 200 && res.data) {
			return {
				success: true,
				message: res.data.message,
				usage: res.data.usage
			};
		} else {
			console.error('[AI] 调用失败:', res);
			throw new Error(res.message || '网络连接失败，请稍后重试');
		}
	}).catch(err => {
		console.error('后端API调用失败:', err);
		throw {
			success: false,
			message: err.message || '网络连接失败，请稍后重试',
			error: err
		};
	});
}

/**
 * 获取菜品推荐
 * @param {String} preference 用户偏好（如：辣的、清淡、海鲜等）
 * @returns {Promise}
 */
export function getDishRecommendation(preference) {
	const prompt = `用户想要${preference}的菜品，请从七云菜馆的菜单中推荐3-5道合适的菜品，并简要说明推荐理由。`
	return chatWithAI(prompt)
}

/**
 * 智能客服问答
 * @param {String} question 用户问题
 * @returns {Promise}
 */
export function askCustomerService(question) {
	const systemPrompt = `你是七云菜馆的客服助手。餐厅信息：
- 营业时间：10:00-22:00
- 地址：市中心美食街88号
- 配送范围：3公里内
- 支付方式：微信支付、支付宝
- 特色菜：招牌红烧肉、清蒸鲈鱼、麻辣香锅
请根据用户问题提供准确的回答。`
	
	return chatWithAI(question, [{
		role: 'system',
		content: systemPrompt
	}])
}
