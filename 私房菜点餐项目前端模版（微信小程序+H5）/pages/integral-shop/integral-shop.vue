<template>
	<view class="shop-page">
		<!-- 顶部积分显示 -->
		<view class="header-bar">
			<view class="my-integral">
				<text class="label">我的积分：</text>
				<text class="points">777</text>
			</view>
			<view class="integral-record" @click="goToIntegral">
				<text>积分明细</text>
				<u-icon name="arrow-right" size="24"></u-icon>
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

		<!-- 商品列表 -->
		<view class="goods-list">
			<view class="goods-item" v-for="(item, index) in goodsList" :key="index" @click="showDetail(item)">
				<view class="goods-image">
					<image :src="item.image" mode="aspectFill"></image>
					<view class="stock-tag" v-if="item.stock < 10">仅剩{{ item.stock }}件</view>
				</view>
				<view class="goods-info">
					<view class="goods-name">{{ item.name }}</view>
					<view class="goods-desc">{{ item.desc }}</view>
					<view class="goods-bottom">
						<view class="goods-price">
							<text class="points">{{ item.points }}</text>
							<text class="unit">积分</text>
							<text class="original-price" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
						</view>
						<view class="exchange-btn" @click.stop="exchange(item)">
							立即兑换
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 兑换弹窗 -->
		<u-popup v-model="showExchangePopup" mode="center" border-radius="20">
			<view class="exchange-popup">
				<view class="popup-title">确认兑换</view>
				<view class="popup-goods">
					<image :src="selectedGoods.image" mode="aspectFill"></image>
					<view class="popup-goods-info">
						<view class="popup-goods-name">{{ selectedGoods.name }}</view>
						<view class="popup-goods-points">{{ selectedGoods.points }} 积分</view>
					</view>
				</view>
				<view class="popup-tips">
					<u-icon name="info-circle" color="#ff9900" size="32"></u-icon>
					<text>兑换后积分将立即扣除，请确认兑换</text>
				</view>
				<view class="popup-actions">
					<view class="popup-btn cancel" @click="showExchangePopup = false">取消</view>
					<view class="popup-btn confirm" @click="confirmExchange">确认兑换</view>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				currentTab: 0,
				tabs: ['全部', '优惠券', '周边礼品', '餐饮券'],
				showExchangePopup: false,
				selectedGoods: {},
				goodsList: [
					{
						id: 1,
						name: '10元优惠券',
						desc: '满50元可用',
						image: '/static/index/integral.jpg',
						points: 100,
						originalPrice: 10,
						stock: 50,
						category: 1
					},
					{
						id: 2,
						name: '20元优惠券',
						desc: '满100元可用',
						image: '/static/index/integral.jpg',
						points: 200,
						originalPrice: 20,
						stock: 30,
						category: 1
					},
					{
						id: 3,
						name: '50元优惠券',
						desc: '满200元可用',
						image: '/static/index/integral.jpg',
						points: 500,
						originalPrice: 50,
						stock: 8,
						category: 1
					},
					{
						id: 4,
						name: '品牌保温杯',
						desc: '304不锈钢，保温12小时',
						image: '/static/index/integralShop.png',
						points: 800,
						originalPrice: 89,
						stock: 15,
						category: 2
					},
					{
						id: 5,
						name: '定制帆布袋',
						desc: '环保材质，印有餐厅LOGO',
						image: '/static/index/integralShop.png',
						points: 300,
						originalPrice: 35,
						stock: 25,
						category: 2
					},
					{
						id: 6,
						name: '免费菜品券',
						desc: '可兑换任意30元以内菜品',
						image: '/static/index/activityCenter.png',
						points: 300,
						originalPrice: 30,
						stock: 20,
						category: 3
					}
				]
			}
		},
		methods: {
			switchTab(index) {
				this.currentTab = index
			},
			goToIntegral() {
				uni.navigateTo({
					url: '/pages/integral/integral'
				})
			},
			showDetail(item) {
				uni.showToast({
					title: '商品详情功能开发中',
					icon: 'none'
				})
			},
			exchange(item) {
				this.selectedGoods = item
				this.showExchangePopup = true
			},
			confirmExchange() {
				// 检查积分是否足够
				if (777 < this.selectedGoods.points) {
					uni.showToast({
						title: '积分不足',
						icon: 'none'
					})
					return
				}
				
				// 检查库存
				if (this.selectedGoods.stock <= 0) {
					uni.showToast({
						title: '库存不足',
						icon: 'none'
					})
					return
				}
				
				this.showExchangePopup = false
				uni.showToast({
					title: '兑换成功！',
					icon: 'success'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	.shop-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 30rpx;
	}

	.header-bar {
		background-color: white;
		padding: 30rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		
		.my-integral {
			.label {
				font-size: 28rpx;
				color: #606266;
			}
			
			.points {
				font-size: 36rpx;
				font-weight: bold;
				color: #EE2F37;
				margin-left: 10rpx;
			}
		}
		
		.integral-record {
			display: flex;
			align-items: center;
			font-size: 26rpx;
			color: #909399;
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

	.goods-list {
		padding: 20rpx 30rpx;
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
		
		.goods-item {
			width: 48%;
			background-color: white;
			border-radius: 16rpx;
			overflow: hidden;
			margin-bottom: 20rpx;
			
			.goods-image {
				width: 100%;
				height: 280rpx;
				position: relative;
				
				image {
					width: 100%;
					height: 100%;
				}
				
				.stock-tag {
					position: absolute;
					top: 10rpx;
					right: 10rpx;
					background-color: rgba(238, 47, 55, 0.9);
					color: white;
					padding: 8rpx 16rpx;
					border-radius: 20rpx;
					font-size: 20rpx;
				}
			}
			
			.goods-info {
				padding: 20rpx;
				
				.goods-name {
					font-size: 28rpx;
					font-weight: bold;
					margin-bottom: 8rpx;
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
				}
				
				.goods-desc {
					font-size: 22rpx;
					color: #909399;
					margin-bottom: 15rpx;
					overflow: hidden;
					text-overflow: ellipsis;
					white-space: nowrap;
				}
				
				.goods-bottom {
					display: flex;
					justify-content: space-between;
					align-items: center;
					
					.goods-price {
						display: flex;
						align-items: baseline;
						
						.points {
							font-size: 32rpx;
							font-weight: bold;
							color: #EE2F37;
						}
						
						.unit {
							font-size: 22rpx;
							color: #EE2F37;
							margin-left: 4rpx;
						}
						
						.original-price {
							font-size: 22rpx;
							color: #909399;
							text-decoration: line-through;
							margin-left: 10rpx;
						}
					}
					
					.exchange-btn {
						background-color: #EE2F37;
						color: white;
						padding: 10rpx 20rpx;
						border-radius: 20rpx;
						font-size: 22rpx;
					}
				}
			}
		}
	}

	.exchange-popup {
		width: 550rpx;
		padding: 40rpx;
		
		.popup-title {
			font-size: 36rpx;
			font-weight: bold;
			text-align: center;
			margin-bottom: 30rpx;
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
				
				.popup-goods-points {
					font-size: 32rpx;
					color: #EE2F37;
					font-weight: bold;
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
			
			.popup-btn {
				flex: 1;
				text-align: center;
				padding: 25rpx;
				border-radius: 12rpx;
				font-size: 28rpx;
				font-weight: bold;
				
				&.cancel {
					background-color: #f5f5f5;
					color: #606266;
				}
				
				&.confirm {
					background-color: #EE2F37;
					color: white;
				}
			}
		}
	}
</style>
