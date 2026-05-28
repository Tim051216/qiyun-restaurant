<template>
	<view class="ai-assistant">
		<!-- AI 助手悬浮按钮 -->
		<view class="ai-float-btn" @click="openAiChat">
			<view class="ai-icon-emoji">🤖</view>
			<view class="ai-badge" v-if="hasNewMessage">新</view>
		</view>
	</view>
</template>

<script>
export default {
	name: 'AiAssistant',
	data() {
		return {
			hasNewMessage: false
		}
	},
	methods: {
		// 打开 AI 聊天页面
		openAiChat() {
			uni.navigateTo({
				url: '/pages/ai-chat/ai-chat'
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.ai-assistant {
	position: fixed;
	z-index: 9999;
}

/* 悬浮按钮 */
.ai-float-btn {
	position: fixed;
	right: 30rpx;
	bottom: 150rpx;
	width: 100rpx;
	height: 100rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 50%;
	box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
	animation: float 3s ease-in-out infinite;
	z-index: 10000;
	
	.ai-icon-emoji {
		font-size: 50rpx;
	}
	
	.ai-badge {
		position: absolute;
		top: 0;
		right: 0;
		background: #ff4d4f;
		color: #fff;
		font-size: 20rpx;
		padding: 4rpx 8rpx;
		border-radius: 20rpx;
	}
}

@keyframes float {
	0%, 100% { transform: translateY(0); }
	50% { transform: translateY(-10rpx); }
}
</style>
