<template>
	<view class="signin-page">
		<!-- 签到卡片 -->
		<view class="signin-card">
			<view class="signin-header">
				<view class="signin-title">每日签到</view>
				<view class="signin-desc">连续签到可获得更多积分奖励</view>
			</view>
			<view class="signin-days">
				<view class="signin-streak">
					<text class="streak-number">{{ consecutiveDays }}</text>
					<text class="streak-unit">天</text>
				</view>
				<view class="streak-text">连续签到</view>
			</view>
		</view>

		<!-- 签到日历 -->
		<view class="calendar-section">
			<view class="calendar-header">
				<view class="month-text">2024年2月</view>
			</view>
			<view class="calendar-grid">
				<view class="calendar-day header" v-for="(day, index) in weekDays" :key="index">
					{{ day }}
				</view>
				<view 
					v-for="(day, index) in calendarDays" 
					:key="index"
					:class="['calendar-day', day.dayClass]"
				>
					<view class="day-number">{{ day.day }}</view>
					<view class="day-status" v-if="day.signed">
						<u-icon name="checkmark-circle-fill" color="#EE2F37" size="32"></u-icon>
					</view>
					<view class="day-reward" v-if="day.reward">+{{ day.reward }}</view>
				</view>
			</view>
		</view>

		<!-- 签到奖励规则 -->
		<view class="reward-section">
			<view class="section-title">签到奖励</view>
			<view class="reward-list">
				<view class="reward-item" v-for="(item, index) in rewards" :key="index">
					<view class="reward-icon">{{ item.icon }}</view>
					<view class="reward-info">
						<view class="reward-title">{{ item.title }}</view>
						<view class="reward-desc">{{ item.desc }}</view>
					</view>
					<view class="reward-points">+{{ item.points }}积分</view>
				</view>
			</view>
		</view>

		<!-- 签到按钮 -->
		<view class="signin-button" v-if="!todaySigned" @click="signIn">
			<view class="button-content">
				<u-icon name="calendar" color="#fff" size="40"></u-icon>
				<text>立即签到</text>
			</view>
		</view>
		<view class="signin-button signed" v-else>
			<view class="button-content">
				<u-icon name="checkmark-circle" color="#fff" size="40"></u-icon>
				<text>今日已签到</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				consecutiveDays: 7,
				todaySigned: false,
				weekDays: ['日', '一', '二', '三', '四', '五', '六'],
				calendarDays: [],
				rewards: [
					{
						icon: '📅',
						title: '每日签到',
						desc: '每天签到可获得',
						points: 5
					},
					{
						icon: '🔥',
						title: '连续3天',
						desc: '连续签到3天额外奖励',
						points: 10
					},
					{
						icon: '⭐',
						title: '连续7天',
						desc: '连续签到7天额外奖励',
						points: 30
					},
					{
						icon: '💎',
						title: '连续30天',
						desc: '连续签到30天额外奖励',
						points: 100
					}
				]
			}
		},
		onLoad() {
			this.generateCalendar()
		},
		methods: {
			generateCalendar() {
				// 生成2月份的日历数据
				const days = []
				const today = 8 // 假设今天是2月8日
				
				// 2月1日是周四，前面需要3个空白
				for (let i = 0; i < 3; i++) {
					days.push({ day: '', signed: false, reward: 0, dayClass: 'empty' })
				}
				
				// 生成2月的天数
				for (let i = 1; i <= 29; i++) {
					const signed = i < today || (i === today && this.todaySigned)
					const reward = signed ? (i % 7 === 0 ? 30 : i % 3 === 0 ? 10 : 5) : 0
					const isToday = i === today
					
					let dayClass = ''
					if (isToday) {
						dayClass = 'today'
					} else if (signed) {
						dayClass = 'signed'
					}
					
					days.push({
						day: i,
						signed: signed,
						reward: reward,
						isToday: isToday,
						dayClass: dayClass
					})
				}
				
				this.calendarDays = days
			},
			signIn() {
				this.todaySigned = true
				this.consecutiveDays++
				this.generateCalendar()
				
				uni.showToast({
					title: '签到成功！获得5积分',
					icon: 'success'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.signin-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 150rpx;
	}

	.signin-card {
		margin: 30rpx;
		background: linear-gradient(135deg, #EE2F37 0%, #ff6b6b 100%);
		border-radius: 20rpx;
		padding: 40rpx;
		color: white;
		box-shadow: 0 8rpx 20rpx rgba(238, 47, 55, 0.3);
		
		.signin-header {
			margin-bottom: 30rpx;
			
			.signin-title {
				font-size: 36rpx;
				font-weight: bold;
				margin-bottom: 10rpx;
			}
			
			.signin-desc {
				font-size: 24rpx;
				opacity: 0.9;
			}
		}
		
		.signin-days {
			text-align: center;
			
			.signin-streak {
				.streak-number {
					font-size: 80rpx;
					font-weight: bold;
				}
				
				.streak-unit {
					font-size: 32rpx;
					margin-left: 10rpx;
				}
			}
			
			.streak-text {
				font-size: 28rpx;
				margin-top: 10rpx;
				opacity: 0.9;
			}
		}
	}

	.calendar-section {
		margin: 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
		
		.calendar-header {
			text-align: center;
			margin-bottom: 30rpx;
			
			.month-text {
				font-size: 32rpx;
				font-weight: bold;
			}
		}
		
		.calendar-grid {
			display: grid;
			grid-template-columns: repeat(7, 1fr);
			gap: 15rpx;
			
			.calendar-day {
				aspect-ratio: 1;
				display: flex;
				flex-direction: column;
				align-items: center;
				justify-content: center;
				border-radius: 12rpx;
				position: relative;
				
				&.header {
					font-size: 24rpx;
					color: #909399;
					font-weight: bold;
				}
				
				&.empty {
					visibility: hidden;
				}
				
				&.today {
					background-color: #fff7e6;
					border: 2px solid #EE2F37;
					
					.day-number {
						color: #EE2F37;
						font-weight: bold;
					}
				}
				
				&.signed {
					background-color: #f5f5f5;
					
					.day-number {
						color: #909399;
					}
				}
				
				.day-number {
					font-size: 28rpx;
					margin-bottom: 5rpx;
				}
				
				.day-status {
					position: absolute;
					top: 5rpx;
					right: 5rpx;
				}
				
				.day-reward {
					font-size: 20rpx;
					color: #EE2F37;
					font-weight: bold;
				}
			}
		}
	}

	.reward-section {
		margin: 30rpx;
		background-color: white;
		border-radius: 20rpx;
		padding: 30rpx;
		
		.section-title {
			font-size: 32rpx;
			font-weight: bold;
			margin-bottom: 30rpx;
		}
		
		.reward-list {
			.reward-item {
				display: flex;
				align-items: center;
				padding: 25rpx 0;
				border-bottom: 1px solid #f0f0f0;
				
				&:last-child {
					border-bottom: none;
				}
				
				.reward-icon {
					font-size: 48rpx;
					margin-right: 20rpx;
				}
				
				.reward-info {
					flex: 1;
					
					.reward-title {
						font-size: 28rpx;
						font-weight: bold;
						margin-bottom: 8rpx;
					}
					
					.reward-desc {
						font-size: 24rpx;
						color: #909399;
					}
				}
				
				.reward-points {
					font-size: 32rpx;
					font-weight: bold;
					color: #EE2F37;
				}
			}
		}
	}

	.signin-button {
		position: fixed;
		bottom: 30rpx;
		left: 30rpx;
		right: 30rpx;
		background-color: #EE2F37;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 8rpx 20rpx rgba(238, 47, 55, 0.3);
		
		&.signed {
			background-color: #909399;
		}
		
		.button-content {
			display: flex;
			align-items: center;
			justify-content: center;
			color: white;
			font-size: 32rpx;
			font-weight: bold;
			
			text {
				margin-left: 15rpx;
			}
		}
	}
</style>
