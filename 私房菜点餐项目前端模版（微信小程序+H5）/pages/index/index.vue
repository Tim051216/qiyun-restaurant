<template>
  <view class="page">
    <view class="hero-card">
      <image class="hero-image" src="/static/index/banner.jpg" mode="aspectFill"></image>
      <view class="hero-body">
        <text class="hero-badge">AI 智能点餐</text>
        <text class="hero-title">七云菜馆</text>
        <text class="hero-desc">管理后台、小程序、监控面板三端联动的餐饮系统演示</text>
        <view class="hero-actions">
          <view class="action action-primary" @tap="goMenu">立即点餐</view>
          <view class="action action-secondary" @tap="goProfile">会员中心</view>
        </view>
      </view>
    </view>

    <view class="metric-row">
      <view class="metric-card">
        <text class="metric-value">12</text>
        <text class="metric-label">在线桌台</text>
      </view>
      <view class="metric-card">
        <text class="metric-value">286</text>
        <text class="metric-label">今日订单</text>
      </view>
      <view class="metric-card">
        <text class="metric-value">98%</text>
        <text class="metric-label">准时出餐率</text>
      </view>
    </view>

    <view class="section">
      <text class="section-title">核心功能</text>
      <view class="feature-list">
        <view class="feature-item feature-red" @tap="goMenu">
          <text class="feature-title">扫码点餐</text>
          <text class="feature-desc">支持桌台点餐、购物车预览与快捷结算。</text>
        </view>
        <view class="feature-item feature-gold">
          <text class="feature-title">营销活动</text>
          <text class="feature-desc">优惠券、套餐组合、节日活动统一运营。</text>
        </view>
        <view class="feature-item feature-blue">
          <text class="feature-title">AI 助手</text>
          <text class="feature-desc">菜品推荐、智能问答与引导式下单体验。</text>
        </view>
        <view class="feature-item feature-green" @tap="goProfile">
          <text class="feature-title">会员体系</text>
          <text class="feature-desc">积分、储值余额与会员权益一站式管理。</text>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">今日推荐</text>
      <view class="recommend-card">
        <image class="recommend-image" src="/static/menu/menulist/gbyd.png" mode="aspectFill"></image>
        <view class="recommend-info">
          <text class="recommend-name">招牌小炒黄牛肉</text>
          <text class="recommend-desc">门店热销招牌菜，适合聚餐分享，也是首页主推菜品。</text>
        </view>
        <text class="recommend-price">￥48</text>
      </view>
      <view class="recommend-card">
        <image class="recommend-image" src="/static/menu/menulist/mpdf.png" mode="aspectFill"></image>
        <view class="recommend-info">
          <text class="recommend-name">麻婆豆腐</text>
          <text class="recommend-desc">经典下饭菜，口味稳定，适合作为点餐演示单品。</text>
        </view>
        <text class="recommend-price">￥22</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    goMenu() {
      uni.switchTab({ url: '/pages/menu/menu' })
    },
    goProfile() {
      uni.switchTab({ url: '/pages/my/my' })
    }
  }
}
</script>

<style lang="scss">
.page {
  padding: 24rpx 24rpx 40rpx;
  background: #f5f7fb;
}

.hero-card {
  background: #ffffff;
  border-radius: 28rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 320rpx;
}

.hero-body {
  padding: 28rpx 24rpx 32rpx;
}

.hero-badge {
  display: inline-block;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #fff1f2;
  color: #e11d48;
  font-size: 22rpx;
}

.hero-title {
  display: block;
  margin-top: 18rpx;
  font-size: 42rpx;
  font-weight: 700;
  color: #0f172a;
}

.hero-desc {
  display: block;
  margin-top: 14rpx;
  color: #64748b;
  font-size: 24rpx;
  line-height: 1.6;
}

.hero-actions {
  margin-top: 24rpx;
}

.action {
  height: 84rpx;
  line-height: 84rpx;
  text-align: center;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.action + .action {
  margin-top: 18rpx;
}

.action-primary {
  background: #ee2f37;
  color: #ffffff;
}

.action-secondary {
  background: #f8fafc;
  color: #0f172a;
  border: 2rpx solid #e2e8f0;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  margin-top: 24rpx;
}

.metric-card {
  width: 208rpx;
  padding: 24rpx 12rpx;
  border-radius: 24rpx;
  background: #ffffff;
  text-align: center;
  box-sizing: border-box;
}

.metric-value {
  display: block;
  color: #ee2f37;
  font-size: 38rpx;
  font-weight: 700;
}

.metric-label {
  display: block;
  margin-top: 10rpx;
  color: #64748b;
  font-size: 22rpx;
}

.section {
  margin-top: 28rpx;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  font-size: 30rpx;
  font-weight: 700;
  color: #0f172a;
}

.feature-list {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.feature-item {
  width: 332rpx;
  min-height: 170rpx;
  margin-bottom: 18rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  box-sizing: border-box;
  color: #ffffff;
}

.feature-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
}

.feature-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 1.6;
}

.feature-red {
  background: linear-gradient(135deg, #ef4444, #be123c);
}

.feature-gold {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.feature-blue {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.feature-green {
  background: linear-gradient(135deg, #10b981, #047857);
}

.recommend-card {
  display: flex;
  align-items: center;
  padding: 18rpx;
  margin-bottom: 16rpx;
  border-radius: 22rpx;
  background: #ffffff;
}

.recommend-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 18rpx;
}

.recommend-info {
  flex: 1;
  margin-left: 18rpx;
}

.recommend-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #0f172a;
}

.recommend-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.5;
  color: #64748b;
}

.recommend-price {
  color: #ee2f37;
  font-size: 30rpx;
  font-weight: 700;
}
</style>
