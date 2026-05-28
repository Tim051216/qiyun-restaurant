<template>
	<view class="flash-sale-page">
		<!-- 顶部倒计时区域 -->
		<view class="countdown-section">
			<view class="countdown-header">
				<view class="header-left">
					<view class="flash-icon">⚡</view>
					<view class="header-text">
						<view class="title">限时秒杀</view>
						<view class="subtitle">{{ currentSession.title }}</view>
					</view>
				</view>
				<view class="countdown-timer">
					<view class="timer-label">距离结束</view>
					<view class="timer-box">
						<text class="timer-item">{{ countdown.hours }}</text>
						<text class="timer-colon">:</text>
						<text class="timer-item">{{ countdown.minutes }}</text>
						<text class="timer-colon">:</text>
						<text class="timer-item">{{ countdown.seconds }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 场次切换 -->
		<view class="session-tabs">
			<scroll-view scroll-x class="session-scroll">
				<view 
					class="session-item" 
					:class="currentSessionIndex === index ? 'active' : ''"
					v-for="(session, index) in sessions" 
					:key="index"
					@click="switchSession(index)"
				>
					<view class="session-time">{{ session.time }}</view>
					<view class="session-status" :class="session.status">
						{{ session.statusText }}
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 秒杀商品列表 -->
		<view class="goods-list">
			<view class="goods-item" v-for="(item, index) in currentGoods" :key="index">
				<view class="goods-image-wrapper">
					<image :src="item.image" mode="aspectFill" class="goods-image"></image>
					<view class="sold-out-mask" v-if="item.stock === 0">
						<text>已抢光</text>
					</view>
					<view class="hot-tag" v-if="item.hot">🔥 热抢</view>
				</view>
				<view class="goods-info">
					<view class="goods-name">{{ item.name }}</view>
					<view class="goods-desc">{{ item.desc }}</view>
					<view class="goods-price-row">
						<view class="price-box">
							<view class="flash-price">
								<text class="price-symbol">¥</text>
								<text class="price-number">{{ item.flashPrice }}</text>
							</view>
							<view class="original-price">¥{{ item.originalPrice }}</view>
						</view>
						<view class="discount-tag">{{ item.discount }}折</view>
					</view>
					<view class="goods-footer">
						<view class="stock-bar">
							<view class="stock-progress">
								<view class="progress-bar" :style="{width: item.soldPercent + '%'}"></view>
							</view>
							<view class="stock-text">已抢{{ item.soldPercent }}%</view>
						</view>
						<view 
							class="buy-btn" 
							:class="item.stock === 0 ? 'disabled' : ''"
							@click="flashBuy(item)"
						>
							{{ item.stock === 0 ? '已抢光' : '立即抢' }}
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 秒杀规则弹窗 -->
		<view class="rule-btn" @click="showRulePopup = true">
			<u-icon name="question-circle" size="40" color="#EE2F37"></u-icon>
		</view>

		<u-popup v-model="showRulePopup" mode="center" border-radius="20" width="80%">
			<view class="rule-popup">
				<view class="popup-title">秒杀规则</view>
				<view class="rule-content">
					<view class="rule-item">
						<view class="rule-number">1</view>
						<view class="rule-text">每个场次限时抢购，数量有限，先到先得</view>
					</view>
					<view class="rule-item">
						<view class="rule-number">2</view>
						<view class="rule-text">每人每场次限购1张优惠券</view>
					</view>
					<view class="rule-item">
						<view class="rule-number">3</view>
						<view class="rule-text">秒杀成功后，优惠券自动发放到账户</view>
					</view>
					<view class="rule-item">
						<view class="rule-number">4</view>
						<view class="rule-text">优惠券有效期为7天，请及时使用</view>
					</view>
					<view class="rule-item">
						<view class="rule-number">5</view>
						<view class="rule-text">秒杀商品不支持退款</view>
					</view>
				</view>
				<view class="popup-btn" @click="showRulePopup = false">我知道了</view>
			</view>
		</u-popup>

		<!-- 抢购确认弹窗 -->
		<u-popup v-model="showBuyPopup" mode="center" border-radius="20" width="80%">
			<view class="buy-popup">
				<view class="popup-title">确认抢购</view>
				<view class="popup-goods">
					<image :src="selectedGoods.image" mode="aspectFill"></image>
					<view class="popup-goods-info">
						<view class="popup-goods-name">{{ selectedGoods.name }}</view>
						<view class="popup-goods-price">
							<text class="flash-price">¥{{ selectedGoods.flashPrice }}</text>
							<text class="original-price">¥{{ selectedGoods.originalPrice }}</text>
						</view>
					</view>
				</view>
				<view class="popup-tips">
					<u-icon name="clock" color="#ff9900" size="32"></u-icon>
					<text>限时秒杀，数量有限，手慢无！</text>
				</view>
				<view class="popup-actions">
					<view class="popup-btn-cancel" @click="showBuyPopup = false">再想想</view>
					<view class="popup-btn-confirm" @click="confirmBuy">立即抢购</view>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				currentSessionIndex: 0,
				showRulePopup: false,
				showBuyPopup: false,
				selectedGoods: {},
				countdown: {
					hours: '02',
					minutes: '30',
					seconds: '45'
				},
				countdownTimer: null,
				sessions: [
					{
						time: '10:00',
						status: 'ended',
						statusText: '已结束',
						endTime: new Date('2024/02/08 10:00:00').getTime()
					},
					{
						time: '12:00',
						status: 'ongoing',
						statusText: '抢购中',
						endTime: new Date('2024/02/08 14:00:00').getTime()
					},
					{
						time: '16:00',
						status: 'upcoming',
						statusText: '即将开始',
						endTime: new Date('2024/02/08 18:00:00').getTime()
					},
					{
						time: '20:00',
						status: 'upcoming',
						statusText: '即将开始',
						endTime: new Date('2024/02/08 22:00:00').getTime()
					}
				],
				goodsList: {
					0: [
						{
							id: 1,
							name: '50元无门槛优惠券',
							desc: '全场通用，无门槛使用',
							image: '/static/index/integral.jpg',
							flashPrice: 9.9,
							originalPrice: 50,
							discount: 2,
							stock: 0,
							totalStock: 100,
							soldPercent: 100,
							hot: false
						}
					],
					1: [
						{
							id: 2,
							name: '30元优惠券',
							desc: '满100元可用',
							image: '/static/index/integral.jpg',
							flashPrice: 5.9,
							originalPrice: 30,
							discount: 2,
							stock: 23,
							totalStock: 200,
							soldPercent: 88,
							hot: true
						},
						{
							id: 3,
							name: '20元优惠券',
							desc: '满50元可用',
							image: '/static/index/integral.jpg',
							flashPrice: 3.9,
							originalPrice: 20,
							discount: 2,
							stock: 56,
							totalStock: 150,
							soldPercent: 63,
							hot: true
						},
						{
							id: 4,
							name: '免费菜品券',
							desc: '可兑换指定菜品',
							image: '/static/index/activityCenter.png',
							flashPrice: 1,
							originalPrice: 25,
							discount: 0.4,
							stock: 89,
							totalStock: 100,
							soldPercent: 11,
							hot: false
						}
					],
					2: [
						{
							id: 5,
							name: '100元超值券',
							desc: '满200元可用',
							image: '/static/index/integral.jpg',
							flashPrice: 19.9,
							originalPrice: 100,
							discount: 2,
							stock: 50,
							totalStock: 50,
							soldPercent: 0,
							hot: false
						}
					],
					3: [
						{
							id: 6,
							name: '夜宵专享券',
							desc: '20:00后使用',
							image: '/static/index/integral.jpg',
							flashPrice: 6.6,
							originalPrice: 30,
							discount: 2.2,
							stock: 80,
							totalStock: 80,
							soldPercent: 0,
							hot: false
						}
					]
				}
			}
		},
		computed: {
			currentSession() {
				return this.sessions[this.currentSessionIndex]
			},
			currentGoods() {
				return this.goodsList[this.currentSessionIndex] || []
			}
		},
		onLoad() {
			this.startCountdown()
		},
		onUnload() {
			if (this.countdownTimer) {
				clearInterval(this.countdownTimer)
			}
		},
		methods: {
			switchSession(index) {
				this.currentSessionIndex = index
				this.startCountdown()
			},
			startCountdown() {
				if (this.countdownTimer) {
					clearInterval(this.countdownTimer)
				}
				
				this.updateCountdown()
				this.countdownTimer = setInterval(() => {
					this.updateCountdown()
				}, 1000)
			},
			updateCountdown() {
				const now = Date.now()
				const endTime = this.currentSession.endTime
				const diff = endTime - now
				
				if (diff <= 0) {
					this.countdown = { hours: '00', minutes: '00', seconds: '00' }
					if (this.countdownTimer) {
						clearInterval(this.countdownTimer)
					}
					return
				}
				
				const hours = Math.floor(diff / (1000 * 60 * 60))
				const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
				const seconds = Math.floor((diff % (1000 * 60)) / 1000)
				
				this.countdown = {
					hours: String(hours).padStart(2, '0'),
					minutes: String(minutes).padStart(2, '0'),
					seconds: String(seconds).padStart(2, '0')
				}
			},
			flashBuy(item) {
				if (item.stock === 0) {
					uni.showToast({
						title: '商品已抢光',
						icon: 'none'
					})
					return
				}
				
				if (this.currentSession.status !== 'ongoing') {
					uni.showToast({
						title: '当前场次未开始',
						icon: 'none'
					})
					return
				}
				
				this.selectedGoods = item
				this.showBuyPopup = true
			},
			confirmBuy() {
				// 模拟抢购
				this.showBuyPopup = false
				
				// 显示加载
				uni.showLoading({
					title: '抢购中...'
				})
				
				setTimeout(() => {
					uni.hideLoading()
					
					// 随机成功或失败
					const success = Math.random() > 0.3
					
					if (success) {
						// 更新库存
						this.selectedGoods.stock--
						this.selectedGoods.soldPercent = Math.round((1 - this.selectedGoods.stock / this.selectedGoods.totalStock) * 100)
						
						uni.showToast({
							title: '抢购成功！',
							icon: 'success'
						})
						
						// 跳转到我的优惠券
						setTimeout(() => {
							uni.showToast({
								title: '优惠券已发放到账户',
								icon: 'success'
							})
						}, 1500)
					} else {
						uni.showToast({
							title: '手慢了，已被抢光',
							icon: 'none'
						})
					}
				}, 1000)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.flash-sale-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 30rpx;
	}

	.countdown-section {
		background: linear-gradient(135deg, #ff6b6b 0%, #EE2F37 100%);
		padding: 40rpx 30rpx;
		
		.countdown-header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			
			.header-left {
				display: flex;
				align-items: center;
				
				.flash-icon {
					font-size: 60rpx;
					margin-right: 20rpx;
				}
				
				.header-text {
					color: white;
					
					.title {
						font-size: 36rpx;
						font-weight: bold;
						margin-bottom: 8rpx;
					}
					
					.subtitle {
						font-size: 24rpx;
						opacity: 0.9;
					}
				}
			}
			
			.countdown-timer {
				text-align: right;
				
				.timer-label {
					font-size: 22rpx;
					color: white;
					opacity: 0.9;
					margin-bottom: 10rpx;
				}
				
				.timer-box {
					display: flex;
					align-items: center;
					
					.timer-item {
						background-color: rgba(0, 0, 0, 0.3);
						color: white;
						padding: 8rpx 12rpx;
						border-radius: 8rpx;
						font-size: 28rpx;
						font-weight: bold;
						min-width: 50rpx;
						text-align: center;
					}
					
					.timer-colon {
						color: white;
						margin: 0 5rpx;
						font-size: 28rpx;
						font-weight: bold;
					}
				}
			}
		}
	}

	.session-tabs {
		background-color: white;
		padding: 20rpx 0;
		
		.session-scroll {
			white-space: nowrap;
			padding: 0 30rpx;
			
			.session-item {
				display: inline-block;
				text-align: center;
				padding: 20rpx 40rpx;
				margin-right: 20rpx;
				border-radius: 12rpx;
				background-color: #f5f5f5;
				
				&.active {
					background-color: #fff7e6;
					border: 2px solid #EE2F37;
					
					.session-time {
						color: #EE2F37;
					}
					
					.session-status {
						color: #EE2F37;
					}
				}
				
				.session-time {
					font-size: 32rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
				}
				
				.session-status {
					font-size: 22rpx;
					
					&.ended {
						color: #909399;
					}
					
					&.ongoing {
						color: #EE2F37;
					}
					
					&.upcoming {
						color: #67c23a;
					}
				}
			}
		}
	}

	.goods-list {
		padding: 20rpx 30rpx;
		
		.goods-item {
			background-color: white;
			border-radius: 16rpx;
			padding: 20rpx;
			margin-bottom: 20rpx;
			display: flex;
			
			.goods-image-wrapper {
				width: 200rpx;
				height: 200rpx;
				position: relative;
				margin-right: 20rpx;
				flex-shrink: 0;
				
				.goods-image {
					width: 100%;
					height: 100%;
					border-radius: 12rpx;
				}
				
				.sold-out-mask {
					position: absolute;
					top: 0;
					left: 0;
					right: 0;
					bottom: 0;
					background-color: rgba(0, 0, 0, 0.6);
					border-radius: 12rpx;
					display: flex;
					align-items: center;
					justify-content: center;
					
					text {
						color: white;
						font-size: 32rpx;
						font-weight: bold;
					}
				}
				
				.hot-tag {
					position: absolute;
					top: 10rpx;
					left: 10rpx;
					background-color: rgba(238, 47, 55, 0.9);
					color: white;
					padding: 6rpx 12rpx;
					border-radius: 8rpx;
					font-size: 20rpx;
				}
			}
			
			.goods-info {
				flex: 1;
				display: flex;
				flex-direction: column;
				justify-content: space-between;
				
				.goods-name {
					font-size: 30rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
				}
				
				.goods-desc {
					font-size: 24rpx;
					color: #909399;
					margin-bottom: 15rpx;
				}
				
				.goods-price-row {
					display: flex;
					align-items: center;
					justify-content: space-between;
					margin-bottom: 15rpx;
					
					.price-box {
						display: flex;
						align-items: baseline;
						
						.flash-price {
							color: #EE2F37;
							
							.price-symbol {
								font-size: 24rpx;
								font-weight: bold;
							}
							
							.price-number {
								font-size: 40rpx;
								font-weight: bold;
							}
						}
						
						.original-price {
							font-size: 24rpx;
							color: #909399;
							text-decoration: line-through;
							margin-left: 15rpx;
						}
					}
					
					.discount-tag {
						background-color: #EE2F37;
						color: white;
						padding: 6rpx 12rpx;
						border-radius: 8rpx;
						font-size: 22rpx;
					}
				}
				
				.goods-footer {
					display: flex;
					align-items: center;
					justify-content: space-between;
					
					.stock-bar {
						flex: 1;
						margin-right: 20rpx;
						
						.stock-progress {
							height: 12rpx;
							background-color: #f0f0f0;
							border-radius: 6rpx;
							overflow: hidden;
							margin-bottom: 8rpx;
							
							.progress-bar {
								height: 100%;
								background: linear-gradient(90deg, #ff6b6b 0%, #EE2F37 100%);
								border-radius: 6rpx;
								transition: width 0.3s;
							}
						}
						
						.stock-text {
							font-size: 22rpx;
							color: #909399;
						}
					}
					
					.buy-btn {
						background-color: #EE2F37;
						color: white;
						padding: 15rpx 40rpx;
						border-radius: 30rpx;
						font-size: 26rpx;
						font-weight: bold;
						white-space: nowrap;
						
						&.disabled {
							background-color: #909399;
						}
					}
				}
			}
		}
	}

	.rule-btn {
		position: fixed;
		right: 30rpx;
		bottom: 100rpx;
		width: 80rpx;
		height: 80rpx;
		background-color: white;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	}

	.rule-popup, .buy-popup {
		padding: 40rpx;
		
		.popup-title {
			font-size: 36rpx;
			font-weight: bold;
			text-align: center;
			margin-bottom: 30rpx;
		}
	}

	.rule-content {
		.rule-item {
			display: flex;
			margin-bottom: 25rpx;
			
			.rule-number {
				width: 40rpx;
				height: 40rpx;
				background-color: #EE2F37;
				color: white;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				font-size: 22rpx;
				margin-right: 15rpx;
				flex-shrink: 0;
			}
			
			.rule-text {
				flex: 1;
				font-size: 26rpx;
				color: #606266;
				line-height: 1.6;
			}
		}
	}

	.popup-btn {
		background-color: #EE2F37;
		color: white;
		text-align: center;
		padding: 25rpx;
		border-radius: 12rpx;
		font-size: 32rpx;
		font-weight: bold;
		margin-top: 30rpx;
	}

	.popup-goods {
		display: flex;
		align-items: center;
		padding: 30rpx;
		background-color: #f5f5f5;
		border-radius: 12rpx;
		margin-bottom: 30rpx;
		
		image {
			width: 120rpx;
			height: 120rpx;
			border-radius: 8rpx;
			margin-right: 20rpx;
		}
		
		.popup-goods-info {
			flex: 1;
			
			.popup-goods-name {
				font-size: 28rpx;
				font-weight: bold;
				margin-bottom: 10rpx;
			}
			
			.popup-goods-price {
				.flash-price {
					font-size: 32rpx;
					color: #EE2F37;
					font-weight: bold;
					margin-right: 15rpx;
				}
				
				.original-price {
					font-size: 24rpx;
					color: #909399;
					text-decoration: line-through;
				}
			}
		}
	}

	.popup-tips {
		display: flex;
		align-items: center;
		padding: 20rpx;
		background-color: #fff7e6;
		border-radius: 8rpx;
		margin-bottom: 30rpx;
		
		text {
			flex: 1;
			font-size: 24rpx;
			color: #ff9900;
			margin-left: 10rpx;
		}
	}

	.popup-actions {
		display: flex;
		gap: 20rpx;
		
		.popup-btn-cancel, .popup-btn-confirm {
			flex: 1;
			text-align: center;
			padding: 25rpx;
			border-radius: 12rpx;
			font-size: 28rpx;
			font-weight: bold;
		}
		
		.popup-btn-cancel {
			background-color: #f5f5f5;
			color: #606266;
		}
		
		.popup-btn-confirm {
			background-color: #EE2F37;
			color: white;
		}
	}
</style>
