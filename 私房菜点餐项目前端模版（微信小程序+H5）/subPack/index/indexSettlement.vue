<template>
	<view class="wrap">
		<!-- 自定义返回按钮 -->
		<view class="back-button" @click="goBack">
			<u-icon name="arrow-left" size="40" color="#303133"></u-icon>
			<text>返回</text>
		</view>
		
		<!-- top start -->
		<!-- @subCurrent if true 外卖 else 堂食-->
		<view v-if="subCurrent == 1">
			<!-- address start-->
			<view class="address">
				<view>
					<view class="u-font-weight">北京市朝阳区万豪公馆7号楼1单元1201</view>
					<view class="u-font-24 u-type-info u-m-t-15">
						<text class="u-m-r-10">Kaiyuan_Q</text>
						<text>188****8888</text>
					</view>
				</view>
				<view>
					<u-icon name="arrow-right" color="#909399"></u-icon>
				</view>
			</view>

			<!-- slot -->
			<u-gap height="20" bg-color="#f3f4f6"></u-gap>
		</view>
		<!-- top end -->

		<!-- list start -->
		<view class="u-flex u-row-between u-m-t-30 u-m-b-30 u-m-l-30 u-m-r-30">
			<view class="u-font-24 u-font-weight u-content-color">
				私房菜（万达广场店）
			</view>
			<view>
				<u-tag :text="subCurrent == 1 ? '外卖' : '堂食'" mode="dark" bg-color="#EE2F37" size="mini" />
			</view>
		</view>
		<view v-for="(item,index) in orderList" :key="index" class="u-flex list-box">
			<view>
				<image class="item-menu-image" :src="item.icon" mode="aspectFill"></image>
			</view>
			<view class="item-menu-name">
				<text class="u-font-26">{{item.name}}</text>
				<view class="u-line-2 u-font-20 u-type-info u-m-t-10 u-m-b-10">
					{{item.desc}}
				</view>
				<view class="u-flex u-row-between">
					<view class="u-font-weight u-font-24" style="color: #EE2F37;">
						<text class="u-font-20">￥</text>
						{{item.price}}
					</view>
					<view class="u-type-info u-font-22">
						x{{item.value}}
					</view>
				</view>
			</view>
		</view>
		<!-- list end -->

		<!-- cell start -->
		<view class="u-cell-box">
			<u-cell-group :border="false">
				<u-cell-item title="用餐人数" :value="form.people ? form.people+'人' : '请选择'" :title-style="titleStyle"
					:value-style="valueStyle" hover-class="none" @click="PopupModal(0)" :border-bottom="false"
					v-if="subCurrent == 0"></u-cell-item>
				<u-cell-item title="配送时间" :value="form.mealsTime ? form.mealsTime : '请选择'" :title-style="titleStyle"
					:value-style="valueStyle" hover-class="none" @click="TimerShow = true" :border-bottom="false"
					v-else></u-cell-item>
				<u-cell-item title="留言" :border-bottom="false" :title-style="titleStyle"
					:value="form.leave ? (form.leave.length > 10 ? form.leave.slice(0, 10) + '...' : form.leave) : '无'"
					:value-style="valueStyle" hover-class="none" @click="PopupModal(1)"></u-cell-item>
			</u-cell-group>
		</view>
		<!-- cell end -->

		<!-- slot -->
		<u-gap height="100"></u-gap>

		<!-- button start -->
		<view class="u-bottom">
			<view class="u-bottom__wrap">
				<view class="u-font-weight u-font-40 u-m-l-20">
					<text class="u-font-24">￥</text>
					{{orderPrice.toFixed(2)}}
				</view>
				<view class="u-bottom__nums">共 {{orderNum}} 件商品</view>
			</view>
			<view class="u-bottom__place" @click="confirmPay">
				确认支付
			</view>
		</view>
		<!-- button end -->

		<!-- popup start -->
		<u-popup v-model="PopupShow" mode="bottom" height="80%" border-radius="14" closeable :mask-close-able="false">
			<view v-if="!PopupPage">
				<view class="u-m-l-30 u-m-r-30 u-m-t-30">
					<view>
						<u-image src="/static/menu/index-dining.png" width="220" height="180"></u-image>
					</view>
					<view class="u-font-34 u-font-weight u-m-t-50 u-m-b-50">请选择用餐人数</view>
					<!-- content -->
					<u-grid :col="4" :border="false" hover-class="none">
						<u-grid-item v-for="item in 12" :key="item" :custom-style="customStyle"
							@click="SelectPeople(item)">
							<view class="u-font-weight">
								<!-- #ifdef MP-WEIXIN -->
								{{item+1}}
								<!-- #endif -->
								<!-- #ifdef H5 -->
								{{item}}
								<!-- #endif -->
							</view>
						</u-grid-item>
					</u-grid>
					<u-gap height="30"></u-gap>
				</view>
			</view>
			<!-- leave -->
			<view v-else>
				<view class="u-m-l-30 u-m-r-30 u-m-t-30">
					<view>
						<u-image src="/static/menu/index-leave.jpg" width="220" height="180"></u-image>
					</view>
					<view class="u-font-34 u-font-weight u-m-t-50 u-m-b-50">快捷标签</view>
					<!-- tags -->
					<u-grid :col="4" :border="false" hover-class="none">
						<u-grid-item v-for="item in tags" :key="item" :custom-style="customStyle"
							@click="SelectTags(item)">
							<view class="u-font-weight">{{item}}</view>
						</u-grid-item>
					</u-grid>
					<view class="u-font-34 u-font-weight u-m-t-50 u-m-b-50">自定义留言</view>
					<u-input type="textarea" placeholder="请填写您的需求" placeholder-style="color: #909399"
						:custom-style="inputStyle" v-model="form.leave"></u-input>
					<u-gap height="30"></u-gap>
				</view>
			</view>
		</u-popup>
		<!-- popup end -->

		<!-- timerSelect -->
		<u-picker v-model="TimerShow" mode="time" :params="TimerParams" @confirm="mealsPicker"></u-picker>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				// 堂食or外卖
				subCurrent: 0,
				form: {
					mealsTime: '',
					people: 0,
					leave: ''
				},
				// orderlist
				orderList: [],
				// orderTotalPrice
				orderPrice: 0,
				// orderNum
				orderNum: 0,
				// u-cell
				valueStyle: {
					fontSize: '26rpx'
				},
				titleStyle: {
					fontWeight: 'bold',
					color: '#333',
					fontSize: '26rpx'
				},
				// popup
				PopupShow: false,
				PopupPage: false,
				// timerSelect
				TimerShow: false,
				TimerParams: {
					year: false,
					month: false,
					day: true,
					hour: true,
					minute: true,
					second: false
				},
				// u-cell
				customStyle: {
					border: "20rpx solid #fff",
					borderRadius: "50rpx",
					padding: '40rpx 0',
					backgroundColor: '#f4f4f5'
				},
				// label
				tags: ['不放辣', '少放辣', '多放辣', '少盐', '不吃蒜', '不吃香菜', '不吃葱', '少油'],
				// input
				inputStyle: {
					backgroundColor: '#f3f4f6',
					borderRadius: "20rpx",
					padding: "30rpx"
				}
			}
		},
		onLoad(param) {
			// determine 外卖 or 堂食
			this.subCurrent = param.subCurrent;
			// obtainStorageMenu
			uni.getStorage({
				key: 'dishData',
				success: (res) => {
					this.orderList = res.data.order;
					this.orderNum = res.data.menuNum;
					this.orderPrice = res.data.menuPrice;
				}
			});
		},
		methods: {
			PopupModal(param) {
				if (param) {
					this.PopupPage = true;
				} else {
					this.PopupPage = false;
				}
				this.PopupShow = true;
			},
			goBack() {
				uni.navigateBack()
			},
			// leave
			SelectTags(param) {
				if (this.form.leave === '') {
					this.form.leave = param;
					return;
				}
				this.form.leave = this.form.leave + '，' + param;
			},
			// NumberDiners
			SelectPeople(param) {
				// #ifdef H5
				this.form.people = param;
				// #endif
				// #ifdef MP-WEIXIN
				this.form.people = param + 1;
				// #endif
				this.PopupShow = false;
			},
			// PickMealsTimer
			mealsPicker(param) {
				this.form.mealsTime = param.day + "日" + ' ' + param.hour + ":" + param.minute;
			},
			// confirmPay
			confirmPay() {
				// 堂食
				if (this.subCurrent == 0) {
					if (!this.form.people) {
						this.$u.toast('请选择用餐人数')
						return;
					}
				}
				// 外卖
				if (this.subCurrent == 1) {
					if (!this.form.mealsTime) {
						this.$u.toast('请选择配送时间')
						return;
					}
				}
				uni.redirectTo({
					url: '/subPack/index/indexPaysuccess'
				})
			}
		}
	}
</script>

<style lang="scss">
	.wrap {
		padding-top: 30rpx;
		
		.back-button {
			display: flex;
			align-items: center;
			margin: 0 30rpx 20rpx 30rpx;
			padding: 15rpx 0;
			
			text {
				font-size: 28rpx;
				color: #303133;
				margin-left: 10rpx;
			}
		}
		
		.address {
			padding: 30rpx;
			display: flex;
			justify-content: space-between;
			align-items: center;
		}

		.u-cell-box {
			border-radius: 30rpx;
			margin: 30rpx 0;
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

		.u-bottom {
			position: fixed;
			bottom: 0;
			width: 100%;
			left: 0;
			right: 0;
			display: flex;
			z-index: 99;

			&__nums {
				border-left: 1px solid #606266;
				font-size: 24rpx;
				margin-left: 20rpx;
				padding-left: 20rpx;
				font-weight: bold;
			}

			&__wrap,
			&__place {
				display: flex;
				color: #fff;
			}

			&__wrap {
				width: 70%;
				padding: 30rpx 0 30rpx 30rpx;
				align-items: center;
				color: #303133;
				border-top: 1px solid #f3f4f6;
				background-color: white;
			}

			&__place {
				background-color: #EE2F37;
				width: 30%;
				text-align: center;
				flex-direction: column;
				justify-content: center;
				font-size: 32rpx;
				font-weight: bold;
			}
		}

		.list-box {
			padding: 20rpx 30rpx;
			margin: 0 30rpx 20rpx 30rpx;
			margin-bottom: 20rpx;
			border-radius: 14rpx;
			box-shadow: 2px 0px 8px 0px rgba(243, 244, 246, 0.95);
		}
	}
</style>