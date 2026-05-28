<template>
	<view class="integral-page">
		<!-- 积分卡片 -->
		<view class="integral-card">
			<view class="integral-card__bg">
				<view class="integral-card__title">我的积分</view>
				<view class="integral-card__points">
					<text class="points-number">777</text>
					<text class="points-unit">分</text>
				</view>
				<view class="integral-card__tips">积分可用于兑换优惠券和礼品</view>
			</view>
		</view>

		<!-- 积分规则 -->
		<view class="rules-section">
			<view class="section-title">
				<u-icon name="info-circle" color="#EE2F37" size="32"></u-icon>
				<text>积分规则</text>
			</view>
			<view class="rules-list">
				<view class="rule-item">
					<view class="rule-icon">💰</view>
					<view class="rule-content">
						<view class="rule-title">消费获取</view>
						<view class="rule-desc">每消费1元可获得1积分</view>
					</view>
				</view>
				<view class="rule-item">
					<view class="rule-icon">📅</view>
					<view class="rule-content">
						<view class="rule-title">每日签到</view>
						<view class="rule-desc">每日签到可获得5-10积分</view>
					</view>
				</view>
				<view class="rule-item">
					<view class="rule-icon">🎁</view>
					<view class="rule-content">
						<view class="rule-title">积分兑换</view>
						<view class="rule-desc">100积分可兑换10元优惠券</view>
					</view>
				</view>
				<view class="rule-item">
					<view class="rule-icon">⏰</view>
					<view class="rule-content">
						<view class="rule-title">有效期</view>
						<view class="rule-desc">积分永久有效，不会过期</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 积分明细 -->
		<view class="detail-section">
			<view class="section-title">
				<u-icon name="list" color="#EE2F37" size="32"></u-icon>
				<text>积分明细</text>
			</view>
			<view class="detail-list">
				<view class="detail-item" v-for="(item, index) in integralList" :key="index">
					<view class="detail-left">
						<view class="detail-title">{{ item.title }}</view>
						<view class="detail-time">{{ item.time }}</view>
					</view>
					<view class="detail-right" :class="item.type === 'add' ? 'add' : 'minus'">
						{{ item.type === 'add' ? '+' : '-' }}{{ item.points }}
					</view>
				</view>
			</view>
		</view>

		<!-- 底部按钮 -->
		<view class="bottom-actions">
			<view class="action-btn primary" @click="goToSignIn">
				<u-icon name="calendar" color="#fff" size="36"></u-icon>
				<text>每日签到</text>
			</view>
			<view class="action-btn secondary" @click="goToIntegralShop">
				<u-icon name="shopping-cart" color="#EE2F37" size="36"></u-icon>
				<text>积分商城</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				integralList: [
					{
						title: '消费获得积分',
						time: '2024-02-08 12:30',
						points: 50,
						type: 'add'
					},
					{
						title: '每日签到',
						time: '2024-02-08 09:00',
						points: 10,
						type: 'add'
					},
					{
						title: '兑换优惠券',
						time: '2024-02-07 18:20',
						points: 100,
						type: 'minus'
					},
					{
						title: '消费获得积分',
						time: '2024-02-07 12:15',
						points: 38,
						type: 'add'
					},
					{
						title: '每日签到',
						time: '2024-02-07 08:45',
						points: 10,
						type: 'add'
					}
				]
			}
		},
		methods: {
			goToSignIn() {
				uni.navigateTo({
					url: '/pages/sign-in/sign-in'
				})
			},
			goToIntegralShop() {
				uni.navigateTo({
					url: '/pages/integral-shop/integral-shop'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.integral-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 120rpx;
	}

	.integral-card {
		margin: 30rpx;
		
		&__bg {
			background: linear-gradient(135deg, #EE2F37 0%, #ff6b6b 100%);
			border-radius: 20rpx;
			padding: 60rpx 40rpx;
			color: white;
			box-shadow: 0 8rpx 20rpx rgba(238, 47, 55, 0.3);
		}
		
		&__title {
			font-size: 28rpx;
			opacity: 0.9;
			margin-bottom: 20rpx;
		}
		
		&__points {
			display: flex;
			align-items: baseline;
			margin-bottom: 20rpx;
			
			.points-number {
				font-size: 80rpx;
				font-weight: bold;
				margin-right: 10rpx;
			}
			
			.points-unit {
				font-size: 32rpx;
			}
		}
		
		&__tips {
			font-size: 24rpx;
			opacity: 0.8;
		}
	}

	.rules-section, .detail-section {
		margin: 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
	}

	.section-title {
		display: flex;
		align-items: center;
		font-size: 32rpx;
		font-weight: bold;
		margin-bottom: 30rpx;
		
		text {
			margin-left: 10rpx;
		}
	}

	.rules-list {
		.rule-item {
			display: flex;
			align-items: center;
			padding: 25rpx 0;
			border-bottom: 1px solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.rule-icon {
				font-size: 48rpx;
				margin-right: 20rpx;
			}
			
			.rule-content {
				flex: 1;
				
				.rule-title {
					font-size: 28rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
				}
				
				.rule-desc {
					font-size: 24rpx;
					color: #909399;
				}
			}
		}
	}

	.detail-list {
		.detail-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 25rpx 0;
			border-bottom: 1px solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.detail-left {
				.detail-title {
					font-size: 28rpx;
					margin-bottom: 8rpx;
				}
				
				.detail-time {
					font-size: 24rpx;
					color: #909399;
				}
			}
			
			.detail-right {
				font-size: 32rpx;
				font-weight: bold;
				
				&.add {
					color: #EE2F37;
				}
				
				&.minus {
					color: #909399;
				}
			}
		}
	}

	.bottom-actions {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		display: flex;
		padding: 20rpx 30rpx;
		background-color: white;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
		
		.action-btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			padding: 25rpx;
			border-radius: 12rpx;
			font-size: 28rpx;
			font-weight: bold;
			
			text {
				margin-left: 10rpx;
			}
			
			&.primary {
				background-color: #EE2F37;
				color: white;
				margin-right: 20rpx;
			}
			
			&.secondary {
				background-color: #fff;
				color: #EE2F37;
				border: 2px solid #EE2F37;
			}
		}
	}
</style>
