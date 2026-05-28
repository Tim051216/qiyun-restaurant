<template>
	<view class="wallet-page">
		<!-- 钱包余额卡片 -->
		<view class="balance-card">
			<view class="balance-header">
				<text class="balance-label">钱包余额</text>
				<text class="balance-unit">¥</text>
			</view>
			<view class="balance-amount">{{ balance }}</view>
			<view class="balance-actions">
				<button class="action-btn recharge-btn" @click="handleRecharge">充值</button>
				<button class="action-btn withdraw-btn" @click="handleWithdraw">提现</button>
			</view>
		</view>

		<!-- 功能菜单 -->
		<view class="menu-section">
			<u-cell-group>
				<u-cell-item icon="coupon" title="优惠券" @click="goToCoupons"></u-cell-item>
				<u-cell-item icon="integral" title="我的积分" :value="points + '分'" @click="goToIntegral"></u-cell-item>
			</u-cell-group>
		</view>

		<!-- 交易记录 -->
		<view class="transaction-section">
			<view class="section-title">交易记录</view>
			<view class="transaction-list">
				<view v-if="transactions.length === 0" class="empty-state">
					<text class="empty-text">暂无交易记录</text>
				</view>
				<view v-else v-for="(item, index) in transactions" :key="index" class="transaction-item">
					<view class="transaction-info">
						<text class="transaction-title">{{ item.title }}</text>
						<text class="transaction-time">{{ item.time }}</text>
					</view>
					<text class="transaction-amount" :class="item.type === 'income' ? 'income' : 'expense'">
						{{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
					</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			balance: '0.00',
			points: 0,
			transactions: []
		}
	},
	onLoad() {
		this.loadWalletData()
	},
	methods: {
		// 加载钱包数据
		loadWalletData() {
			// 从本地存储获取余额
			const balance = uni.getStorageSync('walletBalance') || '0.00'
			this.balance = balance
			
			// 从本地存储获取积分
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo && userInfo.points) {
				this.points = userInfo.points
			}
			
			// 加载交易记录（示例数据）
			this.loadTransactions()
		},
		
		// 加载交易记录
		loadTransactions() {
			// 从本地存储获取交易记录
			const transactions = uni.getStorageSync('transactions') || []
			this.transactions = transactions
		},
		
		// 充值
		handleRecharge() {
			uni.showModal({
				title: '充值',
				content: '请输入充值金额',
				editable: true,
				placeholderText: '请输入金额',
				success: (res) => {
					if (res.confirm && res.content) {
						const amount = parseFloat(res.content)
						if (isNaN(amount) || amount <= 0) {
							uni.showToast({
								title: '请输入有效金额',
								icon: 'none'
							})
							return
						}
						
						// 更新余额
						const newBalance = (parseFloat(this.balance) + amount).toFixed(2)
						this.balance = newBalance
						uni.setStorageSync('walletBalance', newBalance)
						
						// 添加交易记录
						this.addTransaction({
							title: '账户充值',
							amount: amount.toFixed(2),
							type: 'income',
							time: this.formatTime(new Date())
						})
						
						uni.showToast({
							title: '充值成功',
							icon: 'success'
						})
					}
				}
			})
		},
		
		// 提现
		handleWithdraw() {
			if (parseFloat(this.balance) <= 0) {
				uni.showToast({
					title: '余额不足',
					icon: 'none'
				})
				return
			}
			
			uni.showModal({
				title: '提现',
				content: '请输入提现金额',
				editable: true,
				placeholderText: '请输入金额',
				success: (res) => {
					if (res.confirm && res.content) {
						const amount = parseFloat(res.content)
						if (isNaN(amount) || amount <= 0) {
							uni.showToast({
								title: '请输入有效金额',
								icon: 'none'
							})
							return
						}
						
						if (amount > parseFloat(this.balance)) {
							uni.showToast({
								title: '余额不足',
								icon: 'none'
							})
							return
						}
						
						// 更新余额
						const newBalance = (parseFloat(this.balance) - amount).toFixed(2)
						this.balance = newBalance
						uni.setStorageSync('walletBalance', newBalance)
						
						// 添加交易记录
						this.addTransaction({
							title: '账户提现',
							amount: amount.toFixed(2),
							type: 'expense',
							time: this.formatTime(new Date())
						})
						
						uni.showToast({
							title: '提现成功',
							icon: 'success'
						})
					}
				}
			})
		},
		
		// 添加交易记录
		addTransaction(transaction) {
			this.transactions.unshift(transaction)
			// 只保留最近20条记录
			if (this.transactions.length > 20) {
				this.transactions = this.transactions.slice(0, 20)
			}
			uni.setStorageSync('transactions', this.transactions)
		},
		
		// 格式化时间
		formatTime(date) {
			const year = date.getFullYear()
			const month = String(date.getMonth() + 1).padStart(2, '0')
			const day = String(date.getDate()).padStart(2, '0')
			const hours = String(date.getHours()).padStart(2, '0')
			const minutes = String(date.getMinutes()).padStart(2, '0')
			return `${year}-${month}-${day} ${hours}:${minutes}`
		},
		
		// 跳转到优惠券
		goToCoupons() {
			uni.navigateTo({
				url: '/pages/coupon-center/coupon-center'
			})
		},
		
		// 跳转到积分
		goToIntegral() {
			uni.navigateTo({
				url: '/pages/integral/integral'
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.wallet-page {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 40rpx;
}

.balance-card {
	background: linear-gradient(135deg, #EE2F37 0%, #ff6b6b 100%);
	margin: 30rpx;
	padding: 60rpx 40rpx;
	border-radius: 20rpx;
	box-shadow: 0 8rpx 20rpx rgba(238, 47, 55, 0.3);
}

.balance-header {
	display: flex;
	align-items: baseline;
	margin-bottom: 20rpx;
}

.balance-label {
	color: rgba(255, 255, 255, 0.9);
	font-size: 28rpx;
	margin-right: 10rpx;
}

.balance-unit {
	color: #fff;
	font-size: 40rpx;
	font-weight: bold;
}

.balance-amount {
	color: #fff;
	font-size: 80rpx;
	font-weight: bold;
	margin-bottom: 40rpx;
}

.balance-actions {
	display: flex;
	gap: 30rpx;
}

.action-btn {
	flex: 1;
	height: 80rpx;
	line-height: 80rpx;
	border-radius: 40rpx;
	font-size: 32rpx;
	font-weight: bold;
	border: none;
}

.recharge-btn {
	background-color: #fff;
	color: #EE2F37;
}

.withdraw-btn {
	background-color: rgba(255, 255, 255, 0.2);
	color: #fff;
	border: 2rpx solid #fff;
}

.menu-section {
	margin: 30rpx;
	background-color: #fff;
	border-radius: 20rpx;
	overflow: hidden;
}

.transaction-section {
	margin: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
	padding: 0 10rpx;
}

.transaction-list {
	background-color: #fff;
	border-radius: 20rpx;
	overflow: hidden;
}

.empty-state {
	padding: 100rpx 0;
	text-align: center;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
}

.transaction-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
	
	&:last-child {
		border-bottom: none;
	}
}

.transaction-info {
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.transaction-title {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.transaction-time {
	font-size: 24rpx;
	color: #999;
}

.transaction-amount {
	font-size: 36rpx;
	font-weight: bold;
	
	&.income {
		color: #EE2F37;
	}
	
	&.expense {
		color: #666;
	}
}
</style>
