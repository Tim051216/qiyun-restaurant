<template>
	<view class="wrap">
		<!-- 自定义返回按钮 -->
		<view class="back-button" @click="goBack">
			<u-icon name="arrow-left" size="40" color="#303133"></u-icon>
			<text>返回</text>
		</view>
		
		<!-- header start-->
		<view class="u-font-weight u-font-34">私房菜（万达广场店）</view>
		<view class="u-flex u-row-between u-m-t-20 u-m-b-30">
			<view class="wrap__vip">
				堂食
			</view>
			<view class="u-type-info u-font-24">商家已接单 已买单</view>
		</view>
		<view class="table-number">
			<view class="u-m-b-34">餐桌号</view>
			<view class="u-font-70">04</view>
		</view>
		<!-- header end -->

		<!-- content start-->
		<view>
			<view class="u-font-30 u-font-weight u-m-t-30 u-p-b-30 u-border-bottom">点餐详情</view>
			<view v-for="(item,index) in list" :key="index" class="u-flex list-box">
				<view>
					<image class="item-menu-image" :src="item.icon" mode="aspectFill"></image>
				</view>
				<view class="item-menu-name">
					<text class="u-font-26">{{item.name}}</text>
					<view class="u-m-t-10 u-m-b-10 u-line-2 u-font-22 u-font-weight u-type-info">
						{{item.desc}}
					</view>
					<view class="u-flex u-row-between">
						<view class="u-font-weight u-font-24" style="color: #EE2F37;">
							<text class="u-font-20">￥</text>
							{{item.price}}
						</view>
						<view class="u-type-info u-font-22">
							x{{item.num}}
						</view>
					</view>
				</view>
			</view>
			<view class="u-text-right">
				共计 6 件，合计<text class="u-font-22 u-m-l-10" style="color: #EE2F37;">￥</text><text
					class="u-font-weight u-font-34" style="color: #EE2F37;">79</text>
			</view>
		</view>
		<!-- content end -->

		<!-- slot -->
		<u-gap height="20" bg-color="#f3f4f6" margin-top="30"></u-gap>

		<!-- orderinfo start-->
		<view>
			<view class="u-font-30 u-font-weight u-m-t-30 u-p-b-30 u-border-bottom">订单信息</view>
			<view>
				<u-cell-group :border="false">
					<u-cell-item title="订单编号" value="sfc1723683837788514" :arrow="false"></u-cell-item>
					<u-cell-item title="交易单号" value="4200002451202508156823091627" :arrow="false"></u-cell-item>
					<u-cell-item title="支付方式" value="微信支付" :arrow="false"></u-cell-item>
					<u-cell-item title="下单时间" value="2024-08-15 10:00:00" :arrow="false"></u-cell-item>
					<u-cell-item title="订单留言" value='不吃辣，少盐' :arrow="false" :border-bottom="false"></u-cell-item>
				</u-cell-group>
			</view>
		</view>
		<!-- orderinfo end -->
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// orderlist
				list: [{
					icon: '/static/menu/menulist/gbyd.png',
					name: '干煸芸豆',
					desc: '芸豆+麻椒+老干妈+芝麻+辣椒',
					price: 16,
					num: 1
				}, {
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
				}, {
					icon: '/static/menu/menulist/gdt.png',
					name: '疙瘩汤',
					desc: '面粉+鸡蛋+海米',
					price: 16,
					num: 1
				}]
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss">
	.wrap {
		padding: 30rpx;
		position: relative;

		.back-button {
			display: flex;
			align-items: center;
			margin-bottom: 30rpx;
			padding: 15rpx 0;
			
			text {
				font-size: 28rpx;
				color: #303133;
				margin-left: 10rpx;
			}
		}

		&__vip {
			background-color: #EE2F37;
			color: white;
			padding: 5rpx 0;
			width: 100rpx;
			text-align: center;
			border-radius: 7rpx;
			font-size: 20rpx;
		}

		.table-number {
			text-align: center;
			border-radius: 14rpx;
			padding: 50rpx 0;
			font-weight: bold;
			box-shadow: 2px 0px 8px 0px rgba(243, 244, 246, 0.95);

			view:nth-child(2) {
				color: #EE2F37;
			}
		}

		.list-box {
			box-shadow: 2px 0px 8px 0px rgba(243, 244, 246, 0.95);
			padding: 20rpx 30rpx;
			margin-bottom: 20rpx;
			border-radius: 14rpx;
		}

		.item-menu-image {
			width: 100rpx;
			height: 100rpx;
			border-radius: 20rpx;
		}

		.item-menu-name {
			display: flex;
			flex-direction: column;
			margin-left: 20rpx;
			width: 100%;
		}
	}
</style>