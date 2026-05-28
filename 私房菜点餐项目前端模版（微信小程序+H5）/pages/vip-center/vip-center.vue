<template>
	<view class="vip-page">
		<!-- 会员卡片 -->
		<view class="vip-card">
			<view class="vip-card__bg">
				<view class="vip-level">
					<image src="/static/my/avatarurl.jpg" class="avatar"></image>
					<view class="level-info">
						<view class="username">Kaiyuan_Q</view>
						<view class="level-badge">
							<u-tag text="V7" mode="plain" border-color="#FFD700" color="#FFD700" size="mini" shape="circle" />
							<text class="level-name">黄金会员</text>
						</view>
					</view>
				</view>
				<view class="vip-progress">
					<view class="progress-text">
						<text>当前等级</text>
						<text>下一等级</text>
					</view>
					<u-line-progress active-color="#FFD700" inactive-color="rgba(255,255,255,0.3)" percent="40" height="16"></u-line-progress>
					<view class="progress-tips">还需消费 ¥1000 升级到 V8</view>
				</view>
			</view>
		</view>

		<!-- 会员权益 -->
		<view class="benefits-section">
			<view class="section-title">会员权益</view>
			<view class="benefits-grid">
				<view class="benefit-item" v-for="(item, index) in benefits" :key="index">
					<view class="benefit-icon">{{ item.icon }}</view>
					<view class="benefit-name">{{ item.name }}</view>
					<view class="benefit-desc">{{ item.desc }}</view>
				</view>
			</view>
		</view>

		<!-- 等级说明 -->
		<view class="level-section">
			<view class="section-title">等级说明</view>
			<view class="level-list">
				<view class="level-item" v-for="(item, index) in levels" :key="index" :class="item.current ? 'current' : ''">
					<view class="level-badge-large">
						<text class="level-text">{{ item.level }}</text>
					</view>
					<view class="level-info">
						<view class="level-name">{{ item.name }}</view>
						<view class="level-condition">{{ item.condition }}</view>
						<view class="level-benefits">
							<text v-for="(benefit, idx) in item.benefits" :key="idx">{{ benefit }}</text>
						</view>
					</view>
					<view class="level-status" v-if="item.current">
						<u-tag text="当前等级" type="error" size="mini" />
					</view>
				</view>
			</view>
		</view>

		<!-- 成长值记录 -->
		<view class="growth-section">
			<view class="section-title">
				<text>成长值记录</text>
				<view class="view-all" @click="viewAllGrowth">
					<text>查看全部</text>
					<u-icon name="arrow-right" size="24"></u-icon>
				</view>
			</view>
			<view class="growth-list">
				<view class="growth-item" v-for="(item, index) in growthList" :key="index">
					<view class="growth-left">
						<view class="growth-title">{{ item.title }}</view>
						<view class="growth-time">{{ item.time }}</view>
					</view>
					<view class="growth-right" :class="item.type === 'add' ? 'add' : 'minus'">
						{{ item.type === 'add' ? '+' : '-' }}{{ item.value }}
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				benefits: [
					{ icon: '🎁', name: '生日礼', desc: '生日当月送优惠券' },
					{ icon: '💰', name: '折扣优惠', desc: '全场9折优惠' },
					{ icon: '⭐', name: '积分翻倍', desc: '消费积分x1.5' },
					{ icon: '🚀', name: '优先配送', desc: '外卖优先派送' },
					{ icon: '🎫', name: '专属券', desc: '每月专属优惠券' },
					{ icon: '📞', name: '专属客服', desc: 'VIP客服通道' }
				],
				levels: [
					{
						level: 'V1',
						name: '普通会员',
						condition: '注册即可',
						benefits: ['基础积分', '正常价格'],
						current: false
					},
					{
						level: 'V3',
						name: '白银会员',
						condition: '累计消费满500元',
						benefits: ['积分x1.2', '9.5折优惠'],
						current: false
					},
					{
						level: 'V5',
						name: '黄金会员',
						condition: '累计消费满2000元',
						benefits: ['积分x1.5', '9折优惠', '生日礼'],
						current: false
					},
					{
						level: 'V7',
						name: '铂金会员',
						condition: '累计消费满5000元',
						benefits: ['积分x2', '8.5折优惠', '专属客服'],
						current: true
					},
					{
						level: 'V8',
						name: '钻石会员',
						condition: '累计消费满10000元',
						benefits: ['积分x2.5', '8折优惠', '全部权益'],
						current: false
					}
				],
				growthList: [
					{
						title: '消费获得成长值',
						time: '2024-02-08 12:30',
						value: 50,
						type: 'add'
					},
					{
						title: '完善个人信息',
						time: '2024-02-08 09:00',
						value: 20,
						type: 'add'
					},
					{
						title: '消费获得成长值',
						time: '2024-02-07 18:20',
						value: 38,
						type: 'add'
					}
				]
			}
		},
		methods: {
			viewAllGrowth() {
				uni.showToast({
					title: '成长值详情功能开发中',
					icon: 'none'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.vip-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 30rpx;
	}

	.vip-card {
		margin: 30rpx;
		
		&__bg {
			background: linear-gradient(135deg, #8B4513 0%, #D2691E 100%);
			border-radius: 20rpx;
			padding: 40rpx;
			color: white;
			box-shadow: 0 8rpx 20rpx rgba(139, 69, 19, 0.3);
		}
	}

	.vip-level {
		display: flex;
		align-items: center;
		margin-bottom: 40rpx;
		
		.avatar {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			border: 4px solid rgba(255, 255, 255, 0.3);
			margin-right: 20rpx;
		}
		
		.level-info {
			flex: 1;
			
			.username {
				font-size: 36rpx;
				font-weight: bold;
				margin-bottom: 10rpx;
			}
			
			.level-badge {
				display: flex;
				align-items: center;
				
				.level-name {
					font-size: 24rpx;
					margin-left: 10rpx;
					opacity: 0.9;
				}
			}
		}
	}

	.vip-progress {
		.progress-text {
			display: flex;
			justify-content: space-between;
			font-size: 24rpx;
			margin-bottom: 10rpx;
			opacity: 0.9;
		}
		
		.progress-tips {
			font-size: 24rpx;
			margin-top: 10rpx;
			opacity: 0.8;
		}
	}

	.benefits-section, .level-section, .growth-section {
		margin: 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		margin-bottom: 30rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		
		.view-all {
			display: flex;
			align-items: center;
			font-size: 26rpx;
			color: #909399;
			font-weight: normal;
		}
	}

	.benefits-grid {
		display: grid;
		grid-template-columns: repeat(3, 1fr);
		gap: 30rpx;
		
		.benefit-item {
			text-align: center;
			
			.benefit-icon {
				font-size: 60rpx;
				margin-bottom: 15rpx;
			}
			
			.benefit-name {
				font-size: 26rpx;
				font-weight: bold;
				margin-bottom: 8rpx;
			}
			
			.benefit-desc {
				font-size: 22rpx;
				color: #909399;
			}
		}
	}

	.level-list {
		.level-item {
			display: flex;
			align-items: center;
			padding: 30rpx;
			border-radius: 16rpx;
			margin-bottom: 20rpx;
			background-color: #f5f5f5;
			position: relative;
			
			&.current {
				background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
				border: 2px solid #FFD700;
			}
			
			&:last-child {
				margin-bottom: 0;
			}
			
			.level-badge-large {
				width: 100rpx;
				height: 100rpx;
				border-radius: 50%;
				background: linear-gradient(135deg, #8B4513 0%, #D2691E 100%);
				display: flex;
				align-items: center;
				justify-content: center;
				margin-right: 20rpx;
				
				.level-text {
					font-size: 32rpx;
					font-weight: bold;
					color: white;
				}
			}
			
			.level-info {
				flex: 1;
				
				.level-name {
					font-size: 28rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
				}
				
				.level-condition {
					font-size: 24rpx;
					color: #606266;
					margin-bottom: 10rpx;
				}
				
				.level-benefits {
					display: flex;
					flex-wrap: wrap;
					gap: 10rpx;
					
					text {
						font-size: 22rpx;
						color: #909399;
						background-color: rgba(0, 0, 0, 0.05);
						padding: 4rpx 12rpx;
						border-radius: 8rpx;
					}
				}
			}
			
			.level-status {
				position: absolute;
				top: 10rpx;
				right: 10rpx;
			}
		}
	}

	.growth-list {
		.growth-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 25rpx 0;
			border-bottom: 1px solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.growth-left {
				.growth-title {
					font-size: 28rpx;
					margin-bottom: 8rpx;
				}
				
				.growth-time {
					font-size: 24rpx;
					color: #909399;
				}
			}
			
			.growth-right {
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
</style>
