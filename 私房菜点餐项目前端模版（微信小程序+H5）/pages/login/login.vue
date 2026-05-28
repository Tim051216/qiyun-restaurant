<template>
	<view class="login-container">
		<view class="login-box">
			<view class="logo-section">
				<image class="logo" src="/static/index/banner.jpg" mode="aspectFit"></image>
				<view class="app-name">七云菜馆</view>
				<view class="app-slogan">美食与您相伴</view>
			</view>
			
			<view class="login-content">
				<view class="welcome-text">欢迎使用七云菜馆</view>
				<view class="desc-text">一键快速登录，开启美食之旅</view>
				
				<!-- 微信一键登录按钮 -->
				<button 
					class="wechat-login-btn" 
					@click="handleWechatLogin"
				>
					<view class="btn-content">
						<text class="btn-icon">✨</text>
						<text class="btn-text">微信一键登录</text>
					</view>
				</button>
				
				<view class="tips-text">
					<text>登录即表示同意</text>
					<text class="link">《用户协议》</text>
					<text>和</text>
					<text class="link">《隐私政策》</text>
				</view>
				
				<view class="feature-list">
					<view class="feature-item">
						<text class="feature-icon">✓</text>
						<text class="feature-text">无需注册</text>
					</view>
					<view class="feature-item">
						<text class="feature-icon">✓</text>
						<text class="feature-text">快速登录</text>
					</view>
					<view class="feature-item">
						<text class="feature-icon">✓</text>
						<text class="feature-text">安全可靠</text>
					</view>
				</view>
				
				<view class="test-tip">
					<text class="tip-icon">ℹ️</text>
					<text class="tip-text">测试号不支持手机号授权，使用微信登录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { wechatLogin } from '@/api/wechat.js'
import { setToken, setMember } from '@/utils/auth.js'

export default {
	data() {
		return {
			loading: false
		}
	},
	methods: {
		/**
		 * 微信一键登录
		 */
		async handleWechatLogin() {
			uni.showLoading({
				title: '登录中...'
			})
			
			try {
				// 1. 获取微信登录 code
				const loginRes = await new Promise((resolve, reject) => {
					uni.login({
						success: (res) => resolve(res),
						fail: (err) => reject(err)
					})
				})
				
				if (!loginRes.code) {
					throw new Error('获取登录凭证失败')
				}
				
				console.log('[Login] wx.login code:', loginRes.code)
				
				// 2. 调用后端登录接口
				const res = await wechatLogin(loginRes.code)
				
				console.log('[Login] 后端返回:', res)
				
				// 检查返回格式
				if (res.code === 200 && res.data) {
					// 3. 保存 token
					setToken(res.data.token)
					
					// 4. 保存会员信息
					const memberInfo = {
						memberId: res.data.memberId,
						nickname: res.data.nickname || '微信用户',
						avatar: res.data.avatar || '/static/my/avatarurl.jpg',
						phone: res.data.phone || '',
						level: res.data.level || '普通会员',
						points: res.data.points || 0
					}
					
					setMember(memberInfo)
					uni.setStorageSync('userInfo', memberInfo)
					
					console.log('[Login] 登录成功，会员信息:', memberInfo)
					
					uni.hideLoading()
					uni.showToast({
						title: '登录成功',
						icon: 'success'
					})
					
					// 5. 跳转到首页
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/index/index'
						})
					}, 1500)
				} else {
					throw new Error(res.message || '登录失败')
				}
			} catch (error) {
				console.error('[Login] 登录失败:', error)
				uni.hideLoading()
				uni.showToast({
					title: error.message || '登录失败，请重试',
					icon: 'none',
					duration: 2000
				})
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.login-box {
	width: 100%;
	background: #fff;
	border-radius: 40rpx;
	padding: 60rpx 40rpx;
	box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
}

/* Logo 区域 */
.logo-section {
	text-align: center;
	margin-bottom: 80rpx;
	
	.logo {
		width: 200rpx;
		height: 200rpx;
		border-radius: 50%;
		margin-bottom: 30rpx;
		box-shadow: 0 10rpx 30rpx rgba(102, 126, 234, 0.3);
	}
	
	.app-name {
		font-size: 52rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 15rpx;
	}
	
	.app-slogan {
		font-size: 28rpx;
		color: #999;
	}
}

/* 登录内容 */
.login-content {
	.welcome-text {
		font-size: 40rpx;
		font-weight: bold;
		color: #333;
		text-align: center;
		margin-bottom: 15rpx;
	}
	
	.desc-text {
		font-size: 26rpx;
		color: #999;
		text-align: center;
		margin-bottom: 60rpx;
	}
}

/* 微信登录按钮 */
.wechat-login-btn {
	width: 100%;
	height: 110rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 55rpx;
	border: none;
	margin-bottom: 40rpx;
	box-shadow: 0 10rpx 30rpx rgba(102, 126, 234, 0.4);
	transition: all 0.3s;
	
	&:active {
		transform: scale(0.98);
		box-shadow: 0 5rpx 15rpx rgba(102, 126, 234, 0.3);
	}
	
	.btn-content {
		display: flex;
		align-items: center;
		justify-content: center;
		height: 100%;
		
		.btn-icon {
			font-size: 44rpx;
			margin-right: 15rpx;
		}
		
		.btn-text {
			font-size: 32rpx;
			color: #fff;
			font-weight: 500;
		}
	}
}

/* 提示文本 */
.tips-text {
	text-align: center;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 60rpx;
	
	.link {
		color: #667eea;
	}
}

/* 特性列表 */
.feature-list {
	display: flex;
	justify-content: space-around;
	padding: 40rpx 0;
	background: #f8f9fa;
	border-radius: 20rpx;
	margin-bottom: 30rpx;
	
	.feature-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		
		.feature-icon {
			width: 60rpx;
			height: 60rpx;
			background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 32rpx;
			color: #fff;
			margin-bottom: 15rpx;
		}
		
		.feature-text {
			font-size: 24rpx;
			color: #666;
		}
	}
}

/* 测试提示 */
.test-tip {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 20rpx;
	background: #fff3cd;
	border-radius: 10rpx;
	border: 1rpx solid #ffc107;
	
	.tip-icon {
		font-size: 32rpx;
		margin-right: 10rpx;
	}
	
	.tip-text {
		font-size: 24rpx;
		color: #856404;
	}
}
</style>
