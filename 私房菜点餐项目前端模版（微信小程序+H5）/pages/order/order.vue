<template>
	<view>
		<!-- list start -->
		<view class="content" v-for="(item,index) in orderList" :key="index">
			<view class="content__header">
				<view class="flex">
					<view class="content__vip">
						{{item.eatStatus}}
					</view>
					<view class="content__address">
						{{item.address}}
					</view>
				</view>
				<view class="content__status">
					{{item.status}}
				</view>
			</view>
			<view v-for="(item1,index1) in orderList[index].menu" :key="index1" class="menulist" @click="orderDetail">
				<view>
					<image class="item-menu-image" :src="item1.icon" mode="aspectFill"></image>
				</view>
				<view class="item-menu-name">
					<text class="item-menu-name__name">{{item1.name}}</text>
					<view class="item-menu-name__desc u-line-2">
						{{item1.desc}}
					</view>
					<view class="item-menu-price">
						<view class="item-menu-price__color">
							<text class="u-font-20 item-menu-price__text">￥</text>
							{{item1.price}}
						</view>
						<view class="item-menu-price__num">
							x{{item1.num}}
						</view>
					</view>
				</view>
			</view>
			<view class="total-price">共计{{item.num}}件商品，合计：￥{{item.price}}</view>
			<view class="again-btn" @click="oneMore">
				<u-tag text="再来一单" mode="plain" shape="circle" type="info" />
			</view>
		</view>
		<!-- list end -->
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// orderlist
				orderList: [{
					eatStatus: '堂食',
					address: '私房菜（万达广场店）',
					status: '已完成',
					menu: [{
						icon: '/static/menu/menulist/gbyd.png',
						name: '干煸芸豆',
						desc: '芸豆+麻椒+老干妈+芝麻+辣椒',
						price: 16,
						num: 1
					}],
					num: 1,
					price: 16
				}, {
					eatStatus: '外卖',
					address: '北京市朝阳区万豪公馆...',
					status: '已取消',
					menu: [{
						icon: '/static/menu/menulist/tclj.png',
						name: '糖醋里脊',
						desc: '猪肉+醋+糖',
						price: 28,
						num: 1
					}, {
						icon: '/static/menu/menulist/xhscjd.png',
						name: '西红柿炒鸡蛋',
						desc: '西红柿+高筋面粉+鸡蛋+淀粉',
						price: 14,
						num: 1
					}, {
						icon: '/static/menu/menulist/mf.png',
						name: '米饭',
						desc: '五常大米+水',
						price: 2.5,
						num: 2
					}],
					num: 4,
					price: 47
				}]
			}
		},
		methods: {
			// orderdetail
			orderDetail() {
				uni.navigateTo({
					url: "/subPack/order/orderDetail"
				})
			},
			// again
			oneMore() {
				uni.switchTab({
					url: '/pages/menu/menu'
				})
			}
		}
	}
</script>

<style lang="scss">
	.flex {
		display: flex;
	}

	.content {
		margin: 30rpx;
		padding: 20rpx;
		box-shadow: 2px 0px 8px 0px rgba(243, 244, 246, 0.95);

		&__header {
			display: flex;
			justify-content: space-between;
		}

		&__address {
			font-weight: bold;
			font-size: 28rpx;
		}

		&__status {
			font-size: 24rpx;
			color: $u-type-info;
		}

		&__vip {
			background-color: #EE2F37;
			color: white;
			padding: 5rpx 0;
			width: 77rpx;
			text-align: center;
			border-radius: 7rpx;
			font-size: 20rpx;
			margin-right: 10rpx;
		}
	}

	.menulist {
		display: flex;
		margin-top: 30rpx;
	}

	.item-menu-name {
		display: flex;
		flex-direction: column;
		margin-left: 20rpx;
		width: 100%;

		&__name {
			font-size: 26rpx;
			font-weight: bold;
		}

		&__desc {
			margin: 10rpx 0;
			font-size: 22rpx;
			font-weight: bold;
			color: $u-type-info;
		}
	}

	.item-menu-image {
		width: 100rpx;
		height: 100rpx;
		border-radius: 20rpx;
	}

	.item-menu-price {
		display: flex;
		justify-content: space-between;

		&__color {
			font-weight: bold;
			font-size: 24rpx;
			color: #EE2F37;
		}

		&__num {
			color: $u-type-info;
			font-size: 22rpx;
		}
	}

	.total-price {
		text-align: right;
		font-size: 22rpx;
		font-weight: bold;
		margin-top: 30rpx;
	}

	.again-btn {
		display: flex;
		justify-content: flex-end;
		margin-top: 30rpx;
	}
</style>