<template>
	<view class="activity-page">
		<!-- 轮播活动 -->
		<view class="banner-section">
			<u-swiper :list="bannerList" height="360" border-radius="20" indicator-pos="bottomCenter"></u-swiper>
		</view>

		<!-- 活动分类 -->
		<view class="category-section">
			<view 
				class="category-item" 
				:class="currentCategory === index ? 'active' : ''"
				v-for="(item, index) in categories" 
				:key="index"
				@click="switchCategory(index)"
			>
				<view class="category-icon">{{ item.icon }}</view>
				<view class="category-name">{{ item.name }}</view>
			</view>
		</view>

		<!-- 活动列表 -->
		<view class="activity-list">
			<view class="activity-item" v-for="(item, index) in activityList" :key="index" @click="viewDetail(item)">
				<image :src="item.image" mode="aspectFill" class="activity-image"></image>
				<view class="activity-info">
					<view class="activity-header">
						<view class="activity-title">{{ item.title }}</view>
						<view class="activity-tag" :style="{backgroundColor: item.tagColor}">{{ item.tag }}</view>
					</view>
					<view class="activity-desc">{{ item.desc }}</view>
					<view class="activity-time">
						<u-icon name="clock" size="28" color="#909399"></u-icon>
						<text>{{ item.startTime }} - {{ item.endTime }}</text>
					</view>
					<view class="activity-footer">
						<view class="activity-participants">
							<u-icon name="account" size="28" color="#EE2F37"></u-icon>
							<text>{{ item.participants }}人参与</text>
						</view>
						<view class="activity-btn">
							{{ item.status === 'ongoing' ? '立即参与' : item.status === 'upcoming' ? '即将开始' : '已结束' }}
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 活动详情弹窗 -->
		<u-popup v-model="showDetailPopup" mode="bottom" height="80%" border-radius="20" closeable>
			<view class="detail-popup">
				<view class="detail-title">{{ selectedActivity.title }}</view>
				<image :src="selectedActivity.image" mode="aspectFill" class="detail-image"></image>
				<view class="detail-content">
					<view class="detail-section">
						<view class="detail-label">活动时间</view>
						<view class="detail-text">{{ selectedActivity.startTime }} - {{ selectedActivity.endTime }}</view>
					</view>
					<view class="detail-section">
						<view class="detail-label">活动内容</view>
						<view class="detail-text">{{ selectedActivity.content }}</view>
					</view>
					<view class="detail-section">
						<view class="detail-label">参与方式</view>
						<view class="detail-text">{{ selectedActivity.howToJoin }}</view>
					</view>
					<view class="detail-section">
						<view class="detail-label">活动规则</view>
						<view class="detail-text">{{ selectedActivity.rules }}</view>
					</view>
				</view>
				<view class="detail-actions">
					<view class="detail-btn" @click="joinActivity">立即参与</view>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				currentCategory: 0,
				showDetailPopup: false,
				selectedActivity: {},
				bannerList: [
					'/static/menu/banner-1.jpg',
					'/static/menu/banner-2.jpg',
					'/static/menu/banner-3.jpg'
				],
				categories: [
					{ icon: '🔥', name: '全部' },
					{ icon: '💰', name: '优惠' },
					{ icon: '🎁', name: '福利' },
					{ icon: '🎮', name: '互动' }
				],
				allActivities: [
					{
						id: 1,
						title: '新用户专享优惠',
						desc: '首单立减20元，新用户专属福利',
						image: '/static/index/banner.jpg',
						tag: '新人专享',
						tagColor: '#ff6b6b',
						startTime: '2024-02-01',
						endTime: '2024-02-29',
						participants: 1234,
						status: 'ongoing',
						category: '优惠',
						content: '新注册用户首次下单即可享受20元优惠，无门槛使用。',
						howToJoin: '注册账号后，在结算页面自动抵扣。',
						rules: '1. 仅限新用户首单使用\n2. 不与其他优惠同享\n3. 活动期间有效'
					},
					{
						id: 2,
						title: '周末狂欢日',
						desc: '每周六日全场8折，吃得更实惠',
						image: '/static/menu/banner-1.jpg',
						tag: '限时折扣',
						tagColor: '#EE2F37',
						startTime: '2024-02-10',
						endTime: '2024-02-11',
						participants: 856,
						status: 'ongoing',
						category: '优惠',
						content: '每周六日全天，所有菜品享受8折优惠。',
						howToJoin: '周末下单自动享受折扣，无需领券。',
						rules: '1. 仅限周六日使用\n2. 全场菜品参与\n3. 可与会员折扣叠加'
					},
					{
						id: 3,
						title: '情人节特惠套餐',
						desc: '双人套餐立减50元，浪漫约会首选',
						image: '/static/menu/banner-2.jpg',
						tag: '节日活动',
						tagColor: '#ff69b4',
						startTime: '2024-02-14',
						endTime: '2024-02-14',
						participants: 523,
						status: 'upcoming',
						category: '优惠',
						content: '情人节当天，双人套餐立减50元，还送玫瑰花一支。',
						howToJoin: '选择双人套餐，结算时自动减免。',
						rules: '1. 仅限2月14日使用\n2. 仅限双人套餐\n3. 数量有限，先到先得'
					},
					{
						id: 4,
						title: '积分翻倍周',
						desc: '本周消费积分翻倍，快来囤积分',
						image: '/static/menu/banner-3.jpg',
						tag: '积分活动',
						tagColor: '#ffa500',
						startTime: '2024-02-05',
						endTime: '2024-02-11',
						participants: 2341,
						status: 'ongoing',
						category: '福利',
						content: '活动期间，所有消费获得的积分翻倍。',
						howToJoin: '正常消费即可，积分自动翻倍到账。',
						rules: '1. 活动期间有效\n2. 所有消费参与\n3. 积分即时到账'
					},
					{
						id: 5,
						title: '邀请好友送优惠券',
						desc: '邀请1位好友，双方各得20元券',
						image: '/static/index/activityCenter.png',
						tag: '邀请有礼',
						tagColor: '#67c23a',
						startTime: '2024-02-01',
						endTime: '2024-02-29',
						participants: 678,
						status: 'ongoing',
						category: '福利',
						content: '邀请好友注册并下单，双方各获得20元优惠券。',
						howToJoin: '分享邀请链接给好友，好友注册后即可获得。',
						rules: '1. 好友需完成首单\n2. 优惠券7天内有效\n3. 邀请人数不限'
					},
					{
						id: 6,
						title: '每日签到抽奖',
						desc: '连续签到7天，抽取神秘大奖',
						image: '/static/index/integral.jpg',
						tag: '互动游戏',
						tagColor: '#409EFF',
						startTime: '2024-02-01',
						endTime: '2024-02-29',
						participants: 3456,
						status: 'ongoing',
						category: '互动',
						content: '每日签到可获得抽奖机会，连续签到7天额外获得大奖抽奖机会。',
						howToJoin: '进入签到页面完成签到，即可参与抽奖。',
						rules: '1. 每日可签到一次\n2. 连续签到奖励更丰厚\n3. 奖品包括优惠券、积分等'
					}
				]
			}
		},
		computed: {
			activityList() {
				if (this.currentCategory === 0) {
					// 全部
					return this.allActivities
				} else {
					// 根据分类过滤
					const categoryName = this.categories[this.currentCategory].name
					return this.allActivities.filter(item => item.category === categoryName)
				}
			}
		},
		methods: {
			switchCategory(index) {
				this.currentCategory = index
			},
			viewDetail(item) {
				this.selectedActivity = item
				this.showDetailPopup = true
			},
			joinActivity() {
				if (this.selectedActivity.status === 'ended') {
					uni.showToast({
						title: '活动已结束',
						icon: 'none'
					})
					return
				}
				
				if (this.selectedActivity.status === 'upcoming') {
					uni.showToast({
						title: '活动即将开始，敬请期待',
						icon: 'none'
					})
					return
				}
				
				uni.showToast({
					title: '参与成功！',
					icon: 'success'
				})
				this.showDetailPopup = false
			}
		}
	}
</script>

<style lang="scss" scoped>
	.activity-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 30rpx;
	}

	.banner-section {
		padding: 30rpx;
	}

	.category-section {
		display: flex;
		background-color: white;
		padding: 30rpx;
		margin-bottom: 20rpx;
		
		.category-item {
			flex: 1;
			text-align: center;
			
			.category-icon {
				font-size: 48rpx;
				margin-bottom: 10rpx;
			}
			
			.category-name {
				font-size: 24rpx;
				color: #606266;
			}
			
			&.active {
				.category-name {
					color: #EE2F37;
					font-weight: bold;
				}
			}
		}
	}

	.activity-list {
		padding: 0 30rpx;
		
		.activity-item {
			background-color: white;
			border-radius: 20rpx;
			overflow: hidden;
			margin-bottom: 20rpx;
			
			.activity-image {
				width: 100%;
				height: 300rpx;
			}
			
			.activity-info {
				padding: 30rpx;
				
				.activity-header {
					display: flex;
					justify-content: space-between;
					align-items: center;
					margin-bottom: 15rpx;
					
					.activity-title {
						flex: 1;
						font-size: 32rpx;
						font-weight: bold;
					}
					
					.activity-tag {
						padding: 8rpx 16rpx;
						border-radius: 8rpx;
						font-size: 22rpx;
						color: white;
						margin-left: 20rpx;
					}
				}
				
				.activity-desc {
					font-size: 26rpx;
					color: #606266;
					margin-bottom: 15rpx;
				}
				
				.activity-time {
					display: flex;
					align-items: center;
					font-size: 24rpx;
					color: #909399;
					margin-bottom: 20rpx;
					
					text {
						margin-left: 8rpx;
					}
				}
				
				.activity-footer {
					display: flex;
					justify-content: space-between;
					align-items: center;
					
					.activity-participants {
						display: flex;
						align-items: center;
						font-size: 24rpx;
						color: #606266;
						
						text {
							margin-left: 8rpx;
						}
					}
					
					.activity-btn {
						background-color: #EE2F37;
						color: white;
						padding: 12rpx 30rpx;
						border-radius: 20rpx;
						font-size: 24rpx;
					}
				}
			}
		}
	}

	.detail-popup {
		padding: 40rpx;
		
		.detail-title {
			font-size: 36rpx;
			font-weight: bold;
			margin-bottom: 30rpx;
			text-align: center;
		}
		
		.detail-image {
			width: 100%;
			height: 300rpx;
			border-radius: 16rpx;
			margin-bottom: 30rpx;
		}
		
		.detail-content {
			max-height: 600rpx;
			overflow-y: auto;
			
			.detail-section {
				margin-bottom: 30rpx;
				
				.detail-label {
					font-size: 28rpx;
					font-weight: bold;
					margin-bottom: 15rpx;
					color: #303133;
				}
				
				.detail-text {
					font-size: 26rpx;
					color: #606266;
					line-height: 1.6;
					white-space: pre-line;
				}
			}
		}
		
		.detail-actions {
			margin-top: 30rpx;
			
			.detail-btn {
				background-color: #EE2F37;
				color: white;
				text-align: center;
				padding: 25rpx;
				border-radius: 12rpx;
				font-size: 32rpx;
				font-weight: bold;
			}
		}
	}
</style>
