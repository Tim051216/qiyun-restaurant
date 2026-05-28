<template>
  <view class="u-wrap">
    <view class="header">
      <view v-if="subCurrent">
        <view class="header__title">北京市朝阳区万豪公寓 7 号楼 1 单元 1201</view>
        <view class="header__info">
          <text class="header__name">Kaiyuan_Q</text>
          <text>188****8888</text>
        </view>
      </view>
      <view v-else>
        <view class="header__title">北京市朝阳区万达广场 4 楼</view>
        <view class="header__info">距离您 1.2km</view>
      </view>
      <view>
        <u-subsection
          :list="subList"
          :current="subCurrent"
          active-color="#EE2F37"
          mode="subsection"
          height="50"
          :bold="false"
          @change="subChange"
        ></u-subsection>
      </view>
    </view>

    <view v-if="loading" class="loading-wrap">
      <u-loading mode="circle"></u-loading>
      <text>菜单加载中...</text>
    </view>

    <view class="u-menu-wrap" v-else>
      <scroll-view
        scroll-y
        scroll-with-animation
        class="u-tab-view menu-scroll-view"
        :scroll-top="scrollTop"
        :show-scrollbar="false"
        enhanced
      >
        <view
          v-for="(item, index) in tabbar"
          :key="index"
          class="u-tab-item"
          :class="[current == index ? 'u-tab-item-active' : '']"
          :data-current="index"
          @tap.stop="swichMenu(index)"
        >
          <u-image :src="item.image || '/static/menu/menutab/sssc.png'" width="60" height="60" class="menuimg"></u-image>
          <text class="tabMenu-name">{{ item.name }}</text>
        </view>
        <u-gap height="300" bg-color="#FFFFFF"></u-gap>
      </scroll-view>

      <block v-for="(item, index) in tabbar" :key="index">
        <scroll-view scroll-y class="right-box" v-if="current == index" :show-scrollbar="false" enhanced>
          <view class="right-box__swiper">
            <u-swiper :list="bannerList" :height="180"></u-swiper>
          </view>
          <view class="page-view">
            <view class="item-title">
              <text>{{ item.name }}</text>
            </view>
            <view class="class-item">
              <view class="class-item-box" v-for="(item1, index1) in item.foods" :key="index1">
                <image class="item-menu-image" :src="item1.image || '/static/menu/menulist/mpdf.png'" mode="aspectFill"></image>
                <view class="item-menu-name">
                  <text class="item-menu-name__name">{{ item1.name }}</text>
                  <view class="item-menu-name__desc u-line-2">
                    {{ item1.description || item1.desc }}
                  </view>
                  <view class="item-menu-price">
                    <view class="item-menu-price__color">
                      <text class="item-menu-price__text">￥</text>
                      {{ item1.price }}
                    </view>
                    <view>
                      <u-number-box
                        :min="0"
                        :max="100"
                        disabled-input
                        v-model="item1.value"
                        :long-press="false"
                        color="#fff"
                        @plus="plusMenu(item1)"
                        @minus="minusMenu(item1)"
                      ></u-number-box>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          <u-gap height="200" bg-color="#FAFAFA"></u-gap>
        </scroll-view>
      </block>
    </view>

    <view class="u-bottom">
      <view class="u-bottom__wrap" @click="MenuPopup">
        <view class="u-bottom__bags">
          <u-badge :count="menuNum" type="error" :offset="[-5,-10]"></u-badge>
          <u-icon name="/static/menu/bags.png" size="50"></u-icon>
        </view>
        <view class="u-bottom__price">
          <text>￥</text>
          {{ Math.abs(menuPrice) === 0 ? '0.00' : menuPrice.toFixed(2) }}
        </view>
        <view class="u-bottom__nums">共 {{ menuNum }} 件商品</view>
      </view>
      <view class="u-bottom__place" @click="settlement">去结算</view>
    </view>

    <u-popup v-model="PopupShow" mode="bottom" height="70%" border-radius="14">
      <view class="popup-wrap">
        <view class="emptyShop" @click="clickEmptyShop">
          <u-icon name="trash" class="emptyShop__icon"></u-icon>清空购物车
        </view>
        <u-gap height="80"></u-gap>
        <view class="menulist" v-for="(item, index) in SelectMenu" :key="index">
          <image class="item-menu-images" :src="item.image || '/static/menu/menulist/mpdf.png'" mode="aspectFill"></image>
          <view class="item-menu-name">
            <text class="item-menu-name__name">{{ item.name }}</text>
            <view class="item-menu-name__desc">
              {{ item.description || item.desc }}
            </view>
            <view class="item-menu-price">
              <view class="item-menu-price__color">
                <text class="item-menu-price__text">￥</text>
                {{ item.price }}
              </view>
              <view>
                <u-number-box
                  :min="0"
                  :max="100"
                  disabled-input
                  v-model="item.value"
                  :long-press="false"
                  color="#fff"
                  @plus="plusMenu(item)"
                  @minus="minusMenu(item)"
                ></u-number-box>
              </view>
            </view>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getAllDishesGrouped } from '@/api/dish.js'

export default {
  data() {
    return {
      loading: true,
      PopupShow: false,
      tabbar: [],
      scrollTop: 0,
      current: 0,
      menuHeight: 0,
      menuItemHeight: 0,
      menuNum: 0,
      menuPrice: 0,
      SelectMenu: [],
      subList: [{ name: '堂食' }, { name: '外卖' }],
      subCurrent: 0,
      bannerList: [
        '/static/menu/banner-1.jpg',
        '/static/menu/banner-2.jpg',
        '/static/menu/banner-3.jpg'
      ]
    }
  },
  onLoad() {
    this.loadMenuData()
  },
  onUnload() {
    this.clickEmptyShop()
  },
  onShow() {
    this.subCurrent = uni.getStorageSync('subCurrent') || 0
  },
  methods: {
    async loadMenuData() {
      try {
        this.loading = true
        const res = await getAllDishesGrouped()
        if (res.code === 1 || res.code === 200) {
          this.tabbar = res.data.categories.map(category => {
            return {
              id: category.id,
              name: category.name,
              image: category.image,
              foods: category.foods.map(dish => ({
                id: dish.id,
                name: dish.name,
                desc: dish.description,
                description: dish.description,
                price: dish.price,
                image: dish.image,
                icon: dish.image,
                value: 0
              }))
            }
          })
        }
      } catch (error) {
        console.error('加载菜单失败:', error)
        uni.showToast({
          title: '加载菜单失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    subChange(param) {
      this.subCurrent = param
      uni.setStorageSync('subCurrent', param)
    },
    plusMenu(param) {
      this.menuNum++
      this.menuPrice += Number(param.price)

      const found = this.SelectMenu.find(product => product.id == param.id)
      if (!found) {
        this.SelectMenu.push(param)
      }
    },
    minusMenu(param) {
      const product = this.SelectMenu.find(product => product.id == param.id)
      if (product) {
        this.menuNum--
        this.menuPrice -= Number(product.price)
      }
    },
    settlement() {
      if (this.menuNum != 0 && this.menuPrice != 0) {
        this.SelectMenu = this.SelectMenu.filter(item => item.value !== 0)
        const key = {
          order: this.SelectMenu,
          menuPrice: this.menuPrice,
          menuNum: this.menuNum
        }
        uni.setStorage({
          key: 'dishData',
          data: key,
          success: () => {
            uni.navigateTo({
              url: '/subPack/index/indexSettlement?subCurrent=' + this.subCurrent
            })
          }
        })
      }
    },
    clickEmptyShop() {
      this.SelectMenu.forEach(food => {
        food.value = 0
      })
      this.SelectMenu = []
      this.PopupShow = false
      this.menuNum = 0
      this.menuPrice = 0
    },
    MenuPopup() {
      if (this.menuNum != 0 && this.menuPrice != 0) {
        this.PopupShow = true
        this.SelectMenu = this.SelectMenu.filter(item => item.value !== 0)
      }
    },
    async swichMenu(index) {
      if (index == this.current) return
      this.current = index
      if (this.menuHeight == 0 || this.menuItemHeight == 0) {
        await this.getElRect('menu-scroll-view', 'menuHeight')
        await this.getElRect('u-tab-item', 'menuItemHeight')
      }
      this.scrollTop = index * this.menuItemHeight + this.menuItemHeight / 2 - this.menuHeight / 2
    },
    getElRect(elClass, dataVal) {
      return new Promise(resolve => {
        const query = uni.createSelectorQuery().in(this)
        query
          .select('.' + elClass)
          .fields({ size: true }, res => {
            if (!res) {
              setTimeout(() => {
                this.getElRect(elClass, dataVal).then(resolve)
              }, 10)
              return
            }
            this[dataVal] = res.height
            resolve(res)
          })
          .exec()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.u-wrap {
  height: calc(100vh);
  display: flex;
  flex-direction: column;
}

.loading-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-wrap text {
  margin-top: 20rpx;
  color: #909399;
}

.header {
  display: flex;
  justify-content: space-between;
  padding: 30rpx;
}

.header__title {
  font-weight: bold;
}

.header__info {
  font-size: 24rpx;
  color: $u-type-info;
  margin-top: 15rpx;
}

.header__name {
  margin-right: 15rpx;
}

.menuimg {
  margin-bottom: 10rpx;
}

.u-menu-wrap {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.u-tab-view {
  width: 200rpx;
  height: 100%;
  background-color: #ffffff;
}

.tabMenu-name {
  text-align: center;
  padding: 0 10rpx;
}

.u-tab-item {
  height: 160rpx;
  background: #ffffff;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #8d9699;
  font-weight: 400;
  line-height: 1;
}

.u-tab-item-active {
  position: relative;
  color: #ee2f37;
  font-size: 22rpx;
  font-weight: 600;
  background: #fafafa;
}

.u-tab-item-active::before {
  content: "";
  position: absolute;
  border-left: 2px solid #ee2f37;
  height: 160rpx;
  left: 0;
  top: 0;
}

.right-box {
  background-color: #fafafa;
}

.right-box__swiper {
  margin: 0 30rpx;
}

.item-title {
  font-size: 22rpx;
  color: $u-main-color;
  font-weight: bold;
  padding: 16rpx 16rpx 0 16rpx;
}

.page-view {
  padding: 16rpx;
}

.class-item {
  margin-bottom: 30rpx;
  padding: 16rpx;
  border-radius: 8rpx;
}

.class-item-box {
  display: flex;
  background-color: white;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 2px 0px 8px 0px rgba(244, 244, 245, 0.75);
  border-radius: 14rpx;
}

.item-menu-name {
  display: flex;
  flex-direction: column;
  margin-left: 20rpx;
  width: 100%;
}

.item-menu-name__name {
  font-weight: bold;
  font-size: 30rpx;
}

.item-menu-name__desc {
  margin: 20rpx 0;
  font-size: 20rpx;
  color: $u-type-info;
}

.item-menu-price {
  display: flex;
  justify-content: space-between;
}

.item-menu-price__color {
  font-weight: bold;
  font-size: 36rpx;
  color: #ee2f37;
}

.item-menu-price__text {
  font-size: 22rpx;
}

.item-menu-image {
  width: 50%;
  height: 160rpx;
  border-radius: 20rpx;
}

.item-menu-images {
  width: 30%;
  height: 130rpx;
  border-radius: 20rpx;
}

.u-bottom {
  position: fixed;
  z-index: 7777;
  bottom: 30rpx;
  width: 92%;
  left: 0;
  right: 0;
  margin: 0 auto;
  display: flex;
  box-shadow: 2px 0px 8px 0px rgba(200, 201, 204, 0.75);
  border-radius: 14rpx;
  background-color: white;
}

.u-bottom__bags {
  position: relative;
}

.u-bottom__price {
  font-weight: bold;
  font-size: 40rpx;
  margin-left: 20rpx;
}

.u-bottom__price text {
  font-size: 24rpx;
}

.u-bottom__nums {
  border-left: 1px solid #303133;
  font-size: 24rpx;
  margin-left: 20rpx;
  padding-left: 20rpx;
  font-weight: bold;
}

.u-bottom__wrap,
.u-bottom__place {
  display: flex;
  color: #303133;
}

.u-bottom__wrap {
  width: 70%;
  padding: 20rpx 0 20rpx 30rpx;
  align-items: center;
}

.u-bottom__place {
  background-color: #ee2f37;
  width: 30%;
  text-align: center;
  flex-direction: column;
  justify-content: center;
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  border-top-right-radius: 14rpx;
  border-bottom-right-radius: 14rpx;
}

.popup-wrap {
  margin: 20rpx 30rpx 0 30rpx;
  padding-bottom: 30rpx;
}

.emptyShop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  color: $u-type-info;
  font-size: 24rpx;
  border-bottom: 1px solid #f3f4f6;
  z-index: 2;
  padding: 20rpx;
  display: flex;
  justify-content: flex-end;
}

.emptyShop__icon {
  margin-right: 20rpx;
}

.menulist {
  display: flex;
  margin-bottom: 30rpx;
}
</style>
