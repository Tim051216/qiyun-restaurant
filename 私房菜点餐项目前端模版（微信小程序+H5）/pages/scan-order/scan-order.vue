<template>
	<view class="scan-order">
		<view class="header">
			<view class="table-info">
				<text class="table-no">{{ tableNo }}</text>
				<text class="table-desc">桌号</text>
			</view>
		</view>
		
		<view class="menu-container">
			<!-- 菜品列表 -->
			<scroll-view class="dish-list" scroll-y>
				<view v-for="category in categories" :key="category.id" class="category-section">
					<view class="category-title">{{ category.name }}</view>
					<view class="dish-items">
						<view v-for="dish in category.dishes" :key="dish.id" class="dish-item">
							<image class="dish-image" :src="dish.image" mode="aspectFill"></image>
							<view class="dish-info">
								<text class="dish-name">{{ dish.name }}</text>
								<text class="dish-desc">{{ dish.description }}</text>
								<view class="dish-bottom">
									<text class="dish-price">¥{{ dish.price }}</text>
									<view class="dish-actions">
										<view v-if="getCartCount(dish.id) > 0" class="count-control">
											<view class="btn-minus" @click="removeFromCart(dish)">-</view>
											<text class="count">{{ getCartCount(dish.id) }}</text>
											<view class="btn-plus" @click="addToCart(dish)">+</view>
										</view>
										<view v-else class="btn-add" @click="addToCart(dish)">+</view>
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>
		
		<!-- 购物车 -->
		<view class="cart-bar" @click="showCart = true">
			<view class="cart-icon-wrapper">
				<image class="cart-icon" src="/static/menu/bags.png"></image>
				<view v-if="totalCount > 0" class="cart-badge">{{ totalCount }}</view>
			</view>
			<view class="cart-info">
				<text class="cart-price">¥{{ totalPrice }}</text>
				<text class="cart-desc">另需配送费¥0</text>
			</view>
			<view class="cart-btn" @click.stop="submitOrder">去结算</view>
		</view>
		
		<!-- 购物车弹窗 -->
		<view v-if="showCart" class="cart-mask" @click="showCart = false">
			<view class="cart-popup" @click.stop>
				<view class="cart-header">
					<text class="cart-title">已选商品</text>
					<text class="cart-clear" @click="clearCart">清空</text>
				</view>
				<scroll-view class="cart-list" scroll-y>
					<view v-for="item in cartItems" :key="item.id" class="cart-item">
						<text class="item-name">{{ item.name }}</text>
						<text class="item-price">¥{{ item.price }}</text>
						<view class="item-control">
							<view class="btn-minus" @click="removeFromCart(item)">-</view>
							<text class="count">{{ item.count }}</text>
							<view class="btn-plus" @click="addToCart(item)">+</view>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import { getDishesGrouped } from '@/api/dish.js'
import { createOrder } from '@/api/order.js'
import { getTableInfo } from '@/utils/launchParams.js'

export default {
	data() {
		return {
			tableNo: '',
			categories: [],
			cart: {}, // { dishId: { dish, count } }
			showCart: false
		}
	},
	computed: {
		cartItems() {
			return Object.values(this.cart)
		},
		totalCount() {
			return this.cartItems.reduce((sum, item) => sum + item.count, 0)
		},
		totalPrice() {
			return this.cartItems.reduce((sum, item) => sum + item.price * item.count, 0).toFixed(2)
		}
	},
	onLoad(options) {
		// 使用统一的启动参数处理获取桌号
		const tableInfo = getTableInfo(options)
		
		if (tableInfo.tableNumber) {
			this.tableNo = tableInfo.tableNumber
			console.log('[ScanOrder] 桌号:', this.tableNo)
		} else {
			// 没有桌号信息，提示用户
			uni.showToast({
				title: '请先扫码获取桌号',
				icon: 'none'
			})
			
			// 跳转回首页
			setTimeout(() => {
				uni.switchTab({
					url: '/pages/index/index'
				})
			}, 1500)
			return
		}
		
		this.loadDishes()
	},
	methods: {
		async loadDishes() {
			try {
				const res = await getDishesGrouped()
				if (res.code === 1) {
					this.categories = res.data
				}
			} catch (error) {
				console.error('加载菜品失败:', error)
				uni.showToast({
					title: '加载菜品失败',
					icon: 'none'
				})
			}
		},
		getCartCount(dishId) {
			return this.cart[dishId] ? this.cart[dishId].count : 0
		},
		addToCart(dish) {
			if (this.cart[dish.id]) {
				this.cart[dish.id].count++
			} else {
				this.cart[dish.id] = {
					id: dish.id,
					name: dish.name,
					price: dish.price,
					count: 1
				}
			}
			this.$forceUpdate()
		},
		removeFromCart(dish) {
			if (this.cart[dish.id]) {
				this.cart[dish.id].count--
				if (this.cart[dish.id].count === 0) {
					delete this.cart[dish.id]
				}
			}
			this.$forceUpdate()
		},
		clearCart() {
			this.cart = {}
			this.$forceUpdate()
		},
		async submitOrder() {
			if (this.totalCount === 0) {
				uni.showToast({
					title: '请先选择菜品',
					icon: 'none'
				})
				return
			}
			
			try {
				const orderData = {
					tableNo: this.tableNo,
					details: this.cartItems.map(item => ({
						dishId: item.id,
						dishName: item.name,
						price: item.price,
						quantity: item.count,
						amount: item.price * item.count
					})),
					amount: this.totalPrice
				}
				
				const res = await createOrder(orderData)
				if (res.code === 1) {
					uni.showToast({
						title: '下单成功',
						icon: 'success'
					})
					
					// 清空购物车
					this.clearCart()
					
					// 跳转到订单详情
					setTimeout(() => {
						uni.navigateTo({
							url: `/pages/order-detail/order-detail?id=${res.data.id}`
						})
					}, 1500)
				}
			} catch (error) {
				console.error('下单失败:', error)
				uni.showToast({
					title: '下单失败',
					icon: 'none'
				})
			}
		}
	}
}
</script>

<style lang="scss" scoped>
.scan-order {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f5f5f5;
}

.header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 30rpx 30rpx;
	
	.table-info {
		display: flex;
		flex-direction: column;
		align-items: center;
		
		.table-no {
			font-size: 48rpx;
			font-weight: bold;
			color: #fff;
		}
		
		.table-desc {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.8);
			margin-top: 10rpx;
		}
	}
}

.menu-container {
	flex: 1;
	overflow: hidden;
	padding-bottom: 120rpx;
}

.dish-list {
	height: 100%;
}

.category-section {
	margin-bottom: 20rpx;
	
	.category-title {
		padding: 20rpx 30rpx;
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
		background-color: #fff;
	}
}

.dish-items {
	background-color: #fff;
}

.dish-item {
	display: flex;
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
	
	.dish-image {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		margin-right: 20rpx;
	}
	
	.dish-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		
		.dish-name {
			font-size: 30rpx;
			font-weight: 500;
			color: #333;
		}
		
		.dish-desc {
			font-size: 24rpx;
			color: #999;
			margin-top: 10rpx;
		}
		
		.dish-bottom {
			display: flex;
			justify-content: space-between;
			align-items: center;
			
			.dish-price {
				font-size: 32rpx;
				font-weight: bold;
				color: #ff6b6b;
			}
		}
	}
}

.dish-actions {
	.count-control {
		display: flex;
		align-items: center;
		
		.btn-minus,
		.btn-plus {
			width: 48rpx;
			height: 48rpx;
			border-radius: 50%;
			background-color: #667eea;
			color: #fff;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 32rpx;
		}
		
		.count {
			margin: 0 20rpx;
			font-size: 28rpx;
			color: #333;
			min-width: 40rpx;
			text-align: center;
		}
	}
	
	.btn-add {
		width: 48rpx;
		height: 48rpx;
		border-radius: 50%;
		background-color: #667eea;
		color: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 32rpx;
	}
}

.cart-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 100rpx;
	background-color: #fff;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
	
	.cart-icon-wrapper {
		position: relative;
		margin-right: 20rpx;
		
		.cart-icon {
			width: 60rpx;
			height: 60rpx;
		}
		
		.cart-badge {
			position: absolute;
			top: -10rpx;
			right: -10rpx;
			background-color: #ff6b6b;
			color: #fff;
			font-size: 20rpx;
			padding: 4rpx 8rpx;
			border-radius: 20rpx;
			min-width: 32rpx;
			text-align: center;
		}
	}
	
	.cart-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		
		.cart-price {
			font-size: 32rpx;
			font-weight: bold;
			color: #ff6b6b;
		}
		
		.cart-desc {
			font-size: 22rpx;
			color: #999;
		}
	}
	
	.cart-btn {
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		padding: 20rpx 40rpx;
		border-radius: 50rpx;
		font-size: 28rpx;
	}
}

.cart-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: flex-end;
}

.cart-popup {
	width: 100%;
	background-color: #fff;
	border-radius: 20rpx 20rpx 0 0;
	max-height: 60vh;
	
	.cart-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.cart-title {
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
		}
		
		.cart-clear {
			font-size: 28rpx;
			color: #999;
		}
	}
	
	.cart-list {
		max-height: 50vh;
	}
	
	.cart-item {
		display: flex;
		align-items: center;
		padding: 20rpx 30rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.item-name {
			flex: 1;
			font-size: 28rpx;
			color: #333;
		}
		
		.item-price {
			font-size: 28rpx;
			color: #ff6b6b;
			margin-right: 20rpx;
		}
		
		.item-control {
			display: flex;
			align-items: center;
			
			.btn-minus,
			.btn-plus {
				width: 44rpx;
				height: 44rpx;
				border-radius: 50%;
				background-color: #667eea;
				color: #fff;
				display: flex;
				align-items: center;
				justify-content: center;
				font-size: 28rpx;
			}
			
			.count {
				margin: 0 15rpx;
				font-size: 26rpx;
				color: #333;
				min-width: 40rpx;
				text-align: center;
			}
		}
	}
}
</style>
