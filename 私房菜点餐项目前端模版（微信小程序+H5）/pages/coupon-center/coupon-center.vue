<template>
	<view class="coupon-page">
		<!-- 顶部统计 -->
		<view class="header-stats">
			<view class="stat-item">
				<view class="stat-number">{{ availableCoupons }}</view>
				<view class="stat-label">可用优惠券</view>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<view class="stat-number">{{ usedCoupons }}</view>
				<view class="stat-label">已使用</view>
			</view>
			<view class="stat-divider"></view>
			<view class="stat-item">
				<view class="stat-number">{{ expiredCoupons }}</view>
				<view class="stat-label">已过期</view>
			</view>
		</view>

		<!-- 分类标签 -->
		<view class="category-tabs">
			<view 
				class="tab-item" 
				:class="currentTab === index ? 'active' : ''"
				v-for="(tab, index) in tabs" 
				:key="index"
				@click="switchTab(index)"
			>
				{{ tab }}
			</view>
		</view>

		<!-- 优惠券列表 -->
		<view class="coupon-list">
			<view class="coupon-item" v-for="(item, index) in currentCoupons" :key="index">
				<view class="coupon-left" :class="item.status">
					<view class="coupon-amount">
						<text class="amount-symbol">¥</text>
						<text class="amount-number">{{ item.amount }}</text>
					</view>
					<view class="coupon-condition">{{ item.condition }}</view>
				</view>
				<view class="coupon-right">
					<view class="coupon-title">{{ item.title }}</view>
					<view class="coupon-desc">{{ item.desc }}</view>
					<view class="coupon-time">
						<u-icon name="clock" size="24" color="#909399"></u-icon>
						<text>{{ item.validTime }}</text>
					</view>
					<view class="coupon-footer">
						<view class="coupon-type">{{ item.type }}</view>
						<view 
							class="coupon-btn" 
							:class="item.status"
							@click="handleCoupon(item)"
						>
							{{ item.btnText }}
						</view>
					</view>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view class="empty-state" v-if="currentCoupons.length === 0">
				<u-empty mode="coupon" text="暂无优惠券"></u-empty>
			</view>
		</view>

		<!-- 领券规则 -->
		<view class="rule-section">
			<view class="rule-title">领券规则</view>
			<view class="rule-content">
				<view class="rule-item">1. 每张优惠券仅限使用一次</view>
				<view class="rule-item">2. 优惠券不可兑换现金</view>
				<view class="rule-item">3. 优惠券过期后自动失效</view>
				<view class="rule-item">4. 部分优惠券不可与其他优惠同享</view>
				<view class="rule-item">5. 最终解释权归本店所有</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				currentTab: 0,
				tabs: ['可领取', '未使用', '已使用', '已过期'],
				availableCoupons: 8,
				usedCoupons: 3,
				expiredCoupons: 2,
				allCoupons: {
					0: [ // 可领取
						{
							id: 1,
							amount: 20,
							condition: '满50元可用',
							title: '新人专享券',
							desc: '全场通用，新用户专享',
							validTime: '领取后7天内有效',
							type: '全场通用',
							status: 'available',
							btnText: '立即领取'
						},
						{
							id: 2,
							amount: 30,
							condition: '满100元可用',
							title: '周末特惠券',
							desc: '周末使用更优惠',
							validTime: '领取后3天内有效',
							type: '限时优惠',
							status: 'available',
							btnText: '立即领取'
						},
						{
							id: 3,
							amount: 50,
							condition: '满200元可用',
							title: '会员专享券',
							desc: '仅限会员领取使用',
							validTime: '领取后15天内有效',
							type: '会员专享',
							status: 'available',
							btnText: '立即领取'
						},
						{
							id: 4,
							amount: 10,
							condition: '无门槛',
							title: '签到奖励券',
							desc: '连续签到7天可领',
							validTime: '领取后7天内有效',
							type: '签到奖励',
							status: 'available',
							btnText: '立即领取'
						},
						{
							id: 5,
							amount: 15,
							condition: '满80元可用',
							title: '夜宵专享券',
							desc: '20:00后使用',
							validTime: '领取后5天内有效',
							type: '夜宵专享',
							status: 'available',
							btnText: '立即领取'
						}
					],
					1: [ // 未使用
						{
							id: 6,
							amount: 20,
							condition: '满50元可用',
							title: '新人专享券',
							desc: '全场通用，新用户专享',
							validTime: '2024-02-15前有效',
							type: '全场通用',
							status: 'unused',
							btnText: '去使用'
						},
						{
							id: 7,
							amount: 10,
							condition: '无门槛',
							title: '签到奖励券',
							desc: '连续签到7天可领',
							validTime: '2024-02-12前有效',
							type: '签到奖励',
							status: 'unused',
							btnText: '去使用'
						},
						{
							id: 8,
							amount: 30,
							condition: '满100元可用',
							title: '周末特惠券',
							desc: '周末使用更优惠',
							validTime: '2024-02-11前有效',
							type: '限时优惠',
							status: 'unused',
							btnText: '去使用'
						}
					],
					2: [ // 已使用
						{
							id: 9,
							amount: 20,
							condition: '满50元可用',
							title: '新人专享券',
							desc: '全场通用，新用户专享',
							validTime: '已于2024-02-05使用',
							type: '全场通用',
							status: 'used',
							btnText: '已使用'
						},
						{
							id: 10,
							amount: 15,
							condition: '满80元可用',
							title: '夜宵专享券',
							desc: '20:00后使用',
							validTime: '已于2024-02-03使用',
							type: '夜宵专享',
							status: 'used',
							btnText: '已使用'
						},
						{
							id: 11,
							amount: 10,
							condition: '无门槛',
							title: '签到奖励券',
							desc: '连续签到7天可领',
							validTime: '已于2024-02-01使用',
							type: '签到奖励',
							status: 'used',
							btnText: '已使用'
						}
					],
					3: [ // 已过期
						{
							id: 12,
							amount: 30,
							condition: '满100元可用',
							title: '周末特惠券',
							desc: '周末使用更优惠',
							validTime: '已于2024-01-31过期',
							type: '限时优惠',
							status: 'expired',
							btnText: '已过期'
						},
						{
							id: 13,
							amount: 50,
							condition: '满200元可用',
							title: '会员专享券',
							desc: '仅限会员领取使用',
							validTime: '已于2024-01-28过期',
							type: '会员专享',
							status: 'expired',
							btnText: '已过期'
						}
					]
				}
			}
		},
		computed: {
			currentCoupons() {
				return this.allCoupons[this.currentTab] || []
			}
		},
		methods: {
			switchTab(index) {
				this.currentTab = index
			},
			handleCoupon(item) {
				if (item.status === 'available') {
					// 领取优惠券
					uni.showToast({
						title: '领取成功！',
						icon: 'success'
					})
					// 更新统计
					this.availableCoupons--
					// 移动到未使用
					item.status = 'unused'
					item.btnText = '去使用'
					item.validTime = '2024-02-15前有效'
					this.allCoupons[1].unshift(item)
					// 从可领取中移除
					const index = this.allCoupons[0].findIndex(c => c.id === item.id)
					if (index > -1) {
						this.allCoupons[0].splice(index, 1)
					}
				} else if (item.status === 'unused') {
					// 去使用
					uni.switchTab({
						url: '/pages/menu/menu'
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.coupon-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 30rpx;
	}

	.header-stats {
		background-color: white;
		display: flex;
		padding: 40rpx 30rpx;
		
		.stat-item {
			flex: 1;
			text-align: center;
			
			.stat-number {
				font-size: 48rpx;
				font-weight: bold;
				color: #EE2F37;
				margin-bottom: 10rpx;
			}
			
			.stat-label {
				font-size: 24rpx;
				color: #909399;
			}
		}
		
		.stat-divider {
			width: 1px;
			background-color: #f0f0f0;
			margin: 10rpx 0;
		}
	}

	.category-tabs {
		display: flex;
		background-color: white;
		padding: 20rpx 30rpx;
		margin-top: 20rpx;
		
		.tab-item {
			flex: 1;
			text-align: center;
			padding: 15rpx 0;
			font-size: 28rpx;
			color: #606266;
			position: relative;
			
			&.active {
				color: #EE2F37;
				font-weight: bold;
				
				&::after {
					content: '';
					position: absolute;
					bottom: 0;
					left: 50%;
					transform: translateX(-50%);
					width: 60rpx;
					height: 4rpx;
					background-color: #EE2F37;
					border-radius: 2rpx;
				}
			}
		}
	}

	.coupon-list {
		padding: 20rpx 30rpx;
		
		.coupon-item {
			background-color: white;
			border-radius: 16rpx;
			margin-bottom: 20rpx;
			display: flex;
			overflow: hidden;
			position: relative;
			
			&::before {
				content: '';
				position: absolute;
				left: 200rpx;
				top: 0;
				bottom: 0;
				width: 2rpx;
				background-image: linear-gradient(to bottom, #f0f0f0 0%, #f0f0f0 50%, transparent 50%);
				background-size: 2rpx 20rpx;
			}
			
			.coupon-left {
				width: 200rpx;
				padding: 30rpx 20rpx;
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				background: linear-gradient(135deg, #ff6b6b 0%, #EE2F37 100%);
				
				&.used, &.expired {
					background: linear-gradient(135deg, #909399 0%, #606266 100%);
				}
				
				.coupon-amount {
					color: white;
					margin-bottom: 10rpx;
					
					.amount-symbol {
						font-size: 28rpx;
					}
					
					.amount-number {
						font-size: 56rpx;
						font-weight: bold;
					}
				}
				
				.coupon-condition {
					color: white;
					font-size: 22rpx;
					opacity: 0.9;
				}
			}
			
			.coupon-right {
				flex: 1;
				padding: 30rpx 20rpx;
				
				.coupon-title {
					font-size: 30rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
				}
				
				.coupon-desc {
					font-size: 24rpx;
					color: #909399;
					margin-bottom: 15rpx;
				}
				
				.coupon-time {
					display: flex;
					align-items: center;
					font-size: 22rpx;
					color: #909399;
					margin-bottom: 15rpx;
					
					text {
						margin-left: 8rpx;
					}
				}
				
				.coupon-footer {
					display: flex;
					justify-content: space-between;
					align-items: center;
					
					.coupon-type {
						font-size: 22rpx;
						color: #EE2F37;
						background-color: #fff7e6;
						padding: 6rpx 12rpx;
						border-radius: 8rpx;
					}
					
					.coupon-btn {
						padding: 10rpx 30rpx;
						border-radius: 20rpx;
						font-size: 24rpx;
						font-weight: bold;
						
						&.available {
							background-color: #EE2F37;
							color: white;
						}
						
						&.unused {
							background-color: #67c23a;
							color: white;
						}
						
						&.used, &.expired {
							background-color: #f5f5f5;
							color: #909399;
						}
					}
				}
			}
		}
	}

	.empty-state {
		padding: 100rpx 0;
	}

	.rule-section {
		margin: 30rpx;
		background-color: white;
		border-radius: 16rpx;
		padding: 30rpx;
		
		.rule-title {
			font-size: 32rpx;
			font-weight: bold;
			margin-bottom: 20rpx;
		}
		
		.rule-content {
			.rule-item {
				font-size: 26rpx;
				color: #606266;
				line-height: 1.8;
				margin-bottom: 10rpx;
			}
		}
	}
</style>
