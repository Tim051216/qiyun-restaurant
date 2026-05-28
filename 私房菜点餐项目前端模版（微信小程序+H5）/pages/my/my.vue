<template>
  <view class="page">
    <view class="profile-card">
      <image class="profile-card__avatar" src="/static/my/avatarurl.jpg" mode="aspectFill"></image>
      <view class="profile-card__info">
        <text class="profile-card__name">Kaiyuan_Q</text>
        <text class="profile-card__tag">V7 会员 · AI 推荐已开通</text>
      </view>
    </view>

    <view class="wallet-card">
      <view>
        <text class="wallet-card__label">储值余额</text>
        <text class="wallet-card__value">￥428.00</text>
      </view>
      <view class="wallet-card__badge">会员日 9 折</view>
    </view>

    <view class="section">
      <view class="section__title">常用服务</view>
      <view class="service-grid">
        <view class="service-item">
          <text class="service-item__icon">券</text>
          <text class="service-item__label">优惠券</text>
        </view>
        <view class="service-item">
          <text class="service-item__icon">积</text>
          <text class="service-item__label">积分商城</text>
        </view>
        <view class="service-item">
          <text class="service-item__icon">单</text>
          <text class="service-item__label">订单记录</text>
        </view>
        <view class="service-item">
          <text class="service-item__icon">AI</text>
          <text class="service-item__label">智能助手</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__title">权益中心</view>
      <view class="benefit-list">
        <view class="benefit-item">
          <text class="benefit-item__title">会员成长值</text>
          <text class="benefit-item__desc">距离 V8 还差 120 成长值</text>
        </view>
        <view class="benefit-item">
          <text class="benefit-item__title">生日礼券</text>
          <text class="benefit-item__desc">已发放，可在结算时直接使用</text>
        </view>
        <view class="benefit-item">
          <text class="benefit-item__title">客服支持</text>
          <text class="benefit-item__desc">支持订单咨询与售后处理</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {}
</script>

<style lang="scss">
.page {
  padding: 24rpx;
}

.profile-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #0f172a, #334155);
  color: #fff;
}

.profile-card__avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
}

.profile-card__info {
  margin-left: 22rpx;
}

.profile-card__name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
}

.profile-card__tag {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.72);
}

.wallet-card {
  margin-top: 20rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.wallet-card__label {
  display: block;
  color: #9a3412;
  font-size: 22rpx;
}

.wallet-card__value {
  display: block;
  margin-top: 10rpx;
  color: #7c2d12;
  font-size: 42rpx;
  font-weight: 700;
}

.wallet-card__badge {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: #fb923c;
  color: #fff;
  font-size: 22rpx;
}

.section {
  margin-top: 28rpx;
}

.section__title {
  font-size: 30rpx;
  font-weight: 700;
  margin-bottom: 18rpx;
}

.service-grid {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.service-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 10rpx;
  text-align: center;
  width: 160rpx;
  margin-bottom: 16rpx;
  box-sizing: border-box;
}

.service-item__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #fee2e2;
  color: #dc2626;
  font-size: 24rpx;
  font-weight: 700;
}

.service-item__label {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: #475569;
}

.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.benefit-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
}

.benefit-item__title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
}

.benefit-item__desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: #64748b;
}
</style>
