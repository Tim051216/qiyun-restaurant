<template>
	<view class="service-page">
		<!-- 客服信息卡片 -->
		<view class="service-card">
			<view class="service-avatar">
				<u-avatar src="/static/my/avatarurl.jpg" size="120"></u-avatar>
				<view class="online-badge">在线</view>
			</view>
			<view class="service-info">
				<view class="service-name">七云菜馆客服</view>
				<view class="service-desc">为您提供7x24小时服务</view>
				<view class="service-time">
					<u-icon name="clock" size="28" color="#909399"></u-icon>
					<text>工作时间：09:00-22:00</text>
				</view>
			</view>
		</view>

		<!-- 联系方式 -->
		<view class="contact-section">
			<view class="section-title">联系方式</view>
			<view class="contact-list">
				<view class="contact-item" @click="callPhone">
					<view class="contact-left">
						<u-icon name="phone" size="40" color="#EE2F37"></u-icon>
						<view class="contact-info">
							<view class="contact-label">电话咨询</view>
							<view class="contact-value">400-888-8888</view>
						</view>
					</view>
					<u-icon name="arrow-right" size="32" color="#c0c4cc"></u-icon>
				</view>
				
				<view class="contact-item" @click="openWechat">
					<view class="contact-left">
						<u-icon name="weixin" size="40" color="#09BB07"></u-icon>
						<view class="contact-info">
							<view class="contact-label">微信客服</view>
							<view class="contact-value">qiyuncaiguan</view>
						</view>
					</view>
					<u-icon name="arrow-right" size="32" color="#c0c4cc"></u-icon>
				</view>
				
				<view class="contact-item" @click="copyAddress">
					<view class="contact-left">
						<u-icon name="map" size="40" color="#409EFF"></u-icon>
						<view class="contact-info">
							<view class="contact-label">门店地址</view>
							<view class="contact-value">北京市朝阳区xxx街道xxx号</view>
						</view>
					</view>
					<u-icon name="arrow-right" size="32" color="#c0c4cc"></u-icon>
				</view>
			</view>
		</view>

		<!-- 常见问题 -->
		<view class="faq-section">
			<view class="section-title">常见问题</view>
			<view class="faq-list">
				<view 
					class="faq-item" 
					v-for="(item, index) in faqList" 
					:key="index"
					@click="toggleFaq(index)"
				>
					<view class="faq-question">
						<view class="faq-icon">Q</view>
						<text>{{ item.question }}</text>
						<u-icon 
							:name="item.expanded ? 'arrow-up' : 'arrow-down'" 
							size="28" 
							color="#909399"
						></u-icon>
					</view>
					<view class="faq-answer" v-if="item.expanded">
						<view class="faq-icon answer">A</view>
						<text>{{ item.answer }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 在线客服按钮 -->
		<view class="service-btn-wrapper">
			<view class="service-btn" @click="startChat">
				<u-icon name="chat" size="36" color="white"></u-icon>
				<text>在线客服</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				faqList: [
					{
						question: '如何下单点餐？',
						answer: '您可以在"点餐"页面浏览菜品，点击菜品添加到购物车，然后点击"去结算"完成下单。',
						expanded: false
					},
					{
						question: '支持哪些支付方式？',
						answer: '我们支持微信支付、支付宝支付、会员余额支付等多种支付方式。',
						expanded: false
					},
					{
						question: '如何使用优惠券？',
						answer: '在结算页面选择可用的优惠券，系统会自动抵扣相应金额。',
						expanded: false
					},
					{
						question: '积分如何获得和使用？',
						answer: '消费可获得积分，签到也可获得积分。积分可在积分商城兑换商品或优惠券。',
						expanded: false
					},
					{
						question: '会员等级如何提升？',
						answer: '通过消费累计成长值，达到相应成长值即可升级会员等级，享受更多权益。',
						expanded: false
					},
					{
						question: '订单可以取消吗？',
						answer: '未支付的订单可以取消，已支付的订单请联系客服处理。',
						expanded: false
					},
					{
						question: '配送范围和时间？',
						answer: '配送范围为门店周边5公里，配送时间约30-45分钟。',
						expanded: false
					},
					{
						question: '如何申请退款？',
						answer: '在订单详情页点击"申请退款"，填写退款原因，客服会在24小时内处理。',
						expanded: false
					}
				]
			}
		},
		methods: {
			// 拨打电话
			callPhone() {
				uni.makePhoneCall({
					phoneNumber: '4008888888'
				})
			},
			
			// 打开微信
			openWechat() {
				uni.setClipboardData({
					data: 'qiyuncaiguan',
					success: () => {
						uni.showToast({
							title: '微信号已复制',
							icon: 'success'
						})
					}
				})
			},
			
			// 复制地址
			copyAddress() {
				uni.setClipboardData({
					data: '北京市朝阳区xxx街道xxx号',
					success: () => {
						uni.showToast({
							title: '地址已复制',
							icon: 'success'
						})
					}
				})
			},
			
			// 展开/收起常见问题
			toggleFaq(index) {
				this.faqList[index].expanded = !this.faqList[index].expanded
			},
			
			// 开始聊天
			startChat() {
				uni.showToast({
					title: '正在连接客服...',
					icon: 'loading',
					duration: 2000
				})
				
				setTimeout(() => {
					uni.showToast({
						title: '客服聊天功能开发中',
						icon: 'none'
					})
				}, 2000)
			}
		}
	}
</script>

<style lang="scss" scoped>
	.service-page {
		min-height: 100vh;
		background-color: #f5f5f5;
		padding-bottom: 120rpx;
	}

	.service-card {
		background-color: white;
		padding: 40rpx 30rpx;
		display: flex;
		align-items: center;
		
		.service-avatar {
			position: relative;
			margin-right: 30rpx;
			
			.online-badge {
				position: absolute;
				bottom: 0;
				right: 0;
				background-color: #67c23a;
				color: white;
				font-size: 20rpx;
				padding: 4rpx 12rpx;
				border-radius: 20rpx;
			}
		}
		
		.service-info {
			flex: 1;
			
			.service-name {
				font-size: 32rpx;
				font-weight: bold;
				margin-bottom: 10rpx;
			}
			
			.service-desc {
				font-size: 24rpx;
				color: #909399;
				margin-bottom: 15rpx;
			}
			
			.service-time {
				display: flex;
				align-items: center;
				font-size: 22rpx;
				color: #909399;
				
				text {
					margin-left: 8rpx;
				}
			}
		}
	}

	.contact-section, .faq-section {
		margin-top: 20rpx;
		
		.section-title {
			background-color: white;
			padding: 30rpx;
			font-size: 32rpx;
			font-weight: bold;
		}
	}

	.contact-list {
		background-color: white;
		
		.contact-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 30rpx;
			border-bottom: 1px solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.contact-left {
				display: flex;
				align-items: center;
				flex: 1;
				
				.contact-info {
					margin-left: 20rpx;
					
					.contact-label {
						font-size: 28rpx;
						margin-bottom: 8rpx;
					}
					
					.contact-value {
						font-size: 24rpx;
						color: #909399;
					}
				}
			}
		}
	}

	.faq-list {
		background-color: white;
		
		.faq-item {
			border-bottom: 1px solid #f0f0f0;
			
			&:last-child {
				border-bottom: none;
			}
			
			.faq-question {
				display: flex;
				align-items: center;
				padding: 30rpx;
				
				.faq-icon {
					width: 40rpx;
					height: 40rpx;
					background-color: #EE2F37;
					color: white;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					font-size: 24rpx;
					font-weight: bold;
					margin-right: 20rpx;
					flex-shrink: 0;
					
					&.answer {
						background-color: #67c23a;
					}
				}
				
				text {
					flex: 1;
					font-size: 28rpx;
				}
			}
			
			.faq-answer {
				display: flex;
				padding: 0 30rpx 30rpx 30rpx;
				
				text {
					flex: 1;
					font-size: 26rpx;
					color: #606266;
					line-height: 1.6;
				}
			}
		}
	}

	.service-btn-wrapper {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		padding: 20rpx 30rpx;
		background-color: white;
		box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
		
		.service-btn {
			background: linear-gradient(135deg, #ff6b6b 0%, #EE2F37 100%);
			color: white;
			padding: 25rpx;
			border-radius: 50rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 32rpx;
			font-weight: bold;
			
			text {
				margin-left: 15rpx;
			}
		}
	}
</style>
