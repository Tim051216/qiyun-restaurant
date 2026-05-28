<template>
  <view class="page">
    <view class="topbar">
      <view>
        <view class="topbar__title">今日菜单</view>
        <view class="topbar__desc">堂食 / 外卖统一下单体验</view>
      </view>
      <view class="topbar__summary">
        <text class="topbar__summary-label">已选菜品</text>
        <text class="topbar__summary-value">{{ totalCount }} 份</text>
      </view>
    </view>

    <view class="category-tabs">
      <view
        v-for="(category, index) in menuList"
        :key="category.id"
        class="category-tabs__item"
        :class="{ 'category-tabs__item--active': currentIndex === index }"
        @click="currentIndex = index"
      >
        {{ category.name }}
      </view>
    </view>

    <view class="dish-list">
      <view v-for="dish in currentFoods" :key="dish.id" class="dish-card">
        <image class="dish-card__image" :src="dish.icon" mode="aspectFill"></image>
        <view class="dish-card__info">
          <text class="dish-card__name">{{ dish.name }}</text>
          <text class="dish-card__desc">{{ dish.desc }}</text>
          <view class="dish-card__footer">
            <text class="dish-card__price">￥{{ dish.price }}</text>
            <view class="dish-card__stepper">
              <text class="stepper-btn" @click="minusDish(dish)">-</text>
              <text class="stepper-count">{{ dish.value }}</text>
              <text class="stepper-btn stepper-btn--plus" @click="plusDish(dish)">+</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="checkout-bar">
      <view>
        <view class="checkout-bar__count">共 {{ totalCount }} 份菜品</view>
        <view class="checkout-bar__price">￥{{ totalPrice.toFixed(2) }}</view>
      </view>
      <button class="checkout-btn" @click="mockCheckout">提交订单</button>
    </view>
  </view>
</template>

<script>
import menuList from '@/common/menu.js'

export default {
  data() {
    return {
      currentIndex: 0,
      menuList: JSON.parse(JSON.stringify(menuList))
    }
  },
  computed: {
    currentFoods() {
      return this.menuList[this.currentIndex].foods
    },
    totalCount() {
      return this.menuList.reduce((sum, category) => {
        return sum + category.foods.reduce((inner, dish) => inner + dish.value, 0)
      }, 0)
    },
    totalPrice() {
      return this.menuList.reduce((sum, category) => {
        return sum + category.foods.reduce((inner, dish) => inner + dish.value * Number(dish.price), 0)
      }, 0)
    }
  },
  methods: {
    plusDish(dish) {
      dish.value += 1
    },
    minusDish(dish) {
      if (dish.value > 0) {
        dish.value -= 1
      }
    },
    mockCheckout() {
      uni.showToast({
        title: this.totalCount ? '订单已加入演示购物车' : '请先选择菜品',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss">
.page {
  padding: 24rpx 24rpx 180rpx;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18rpx 8rpx 12rpx;
}

.topbar__title {
  font-size: 40rpx;
  font-weight: 700;
}

.topbar__desc {
  margin-top: 8rpx;
  color: #64748b;
  font-size: 22rpx;
}

.topbar__summary {
  min-width: 120rpx;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: #fff1f2;
  text-align: center;
}

.topbar__summary-label {
  display: block;
  color: #e11d48;
  font-size: 20rpx;
}

.topbar__summary-value {
  display: block;
  margin-top: 8rpx;
  color: #be123c;
  font-size: 28rpx;
  font-weight: 700;
}

.category-tabs {
  display: flex;
  overflow-x: auto;
  white-space: nowrap;
  margin: 18rpx 0 24rpx;
}

.category-tabs__item {
  padding: 18rpx 26rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #475569;
  font-size: 24rpx;
  margin-right: 16rpx;
}

.category-tabs__item--active {
  background: #ee2f37;
  color: #fff;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.dish-card {
  display: flex;
  background: #fff;
  border-radius: 24rpx;
  padding: 18rpx;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.06);
}

.dish-card__image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 18rpx;
}

.dish-card__info {
  flex: 1;
  margin-left: 18rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-card__name {
  font-size: 30rpx;
  font-weight: 700;
}

.dish-card__desc {
  margin-top: 10rpx;
  color: #64748b;
  font-size: 22rpx;
  line-height: 1.5;
}

.dish-card__footer {
  margin-top: 18rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-card__price {
  color: #ee2f37;
  font-size: 30rpx;
  font-weight: 700;
}

.dish-card__stepper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 160rpx;
}

.stepper-btn {
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  border-radius: 50%;
  background: #e2e8f0;
  color: #1e293b;
  text-align: center;
  font-size: 30rpx;
  font-weight: 700;
}

.stepper-btn--plus {
  background: #ee2f37;
  color: #fff;
}

.stepper-count {
  min-width: 24rpx;
  text-align: center;
  font-size: 26rpx;
  font-weight: 600;
}

.checkout-bar {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: 28rpx;
  padding: 22rpx 24rpx;
  border-radius: 28rpx;
  background: #0f172a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 20rpx 40rpx rgba(15, 23, 42, 0.24);
}

.checkout-bar__count {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.72);
}

.checkout-bar__price {
  margin-top: 8rpx;
  font-size: 36rpx;
  font-weight: 700;
}

.checkout-btn {
  width: 220rpx;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ef4444, #be123c);
  color: #fff;
  font-size: 28rpx;
  font-weight: 700;
  padding: 0;
}

.checkout-btn::after {
  border: none;
}
</style>
