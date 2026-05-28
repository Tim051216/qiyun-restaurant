# 小程序API集成说明

## 概述
小程序现在已经集成后端API，所有数据都从MySQL数据库读取，确保数据一致性。

## 已创建的文件

### 1. API请求工具
- **文件**: `utils/request.js`
- **功能**: 封装uni.request，统一处理API请求和错误

### 2. API接口文件
- **文件**: `api/dish.js` - 菜品相关API
- **文件**: `api/activity.js` - 活动相关API  
- **文件**: `api/coupon.js` - 优惠券相关API

### 3. 新版页面
- **文件**: `pages/menu/menu-new.vue` - 从后端加载菜单数据的新版菜单页面

## API配置

### 后端地址
```javascript
const BASE_URL = 'http://localhost:8080'
```

**注意**: 
- 开发环境使用localhost
- 生产环境需要修改为实际服务器地址
- 小程序需要在微信公众平台配置服务器域名白名单

## 数据流程

### 菜单页面数据流程
1. 小程序启动 → 调用 `getAllDishesGrouped()` API
2. 后端从数据库查询所有分类和菜品
3. 后端按分类分组返回数据
4. 小程序接收数据并渲染页面

### 数据格式示例
```json
{
  "code": 1,
  "msg": "操作成功",
  "data": {
    "categories": [
      {
        "id": 1,
        "name": "川菜",
        "image": "/images/category1.jpg",
        "foods": [
          {
            "id": 1,
            "name": "宫保鸡丁",
            "description": "经典川菜",
            "price": 38.00,
            "image": "/images/dish1.jpg",
            "sales": 235,
            "status": 1
          }
        ]
      }
    ]
  }
}
```

## 如何使用新版菜单页面

### 方法1: 替换原有页面
将 `pages/menu/menu.vue` 重命名为 `menu-old.vue`（备份）
将 `pages/menu/menu-new.vue` 重命名为 `menu.vue`

### 方法2: 修改pages.json
在 `pages.json` 中修改菜单页面路径:
```json
{
  "path": "pages/menu/menu-new",
  "style": {
    "navigationBarTitleText": "点餐"
  }
}
```

## 其他页面集成建议

### 活动中心页面
```javascript
// 在 pages/activity-center/activity-center.vue 中
import { getActivityList } from '@/api/activity.js'

async loadActivities() {
  const res = await getActivityList({ page: 1, size: 20 })
  this.allActivities = res.data.records
}
```

### 优惠券中心页面
```javascript
// 在 pages/coupon-center/coupon-center.vue 中
import { getCouponList } from '@/api/coupon.js'

async loadCoupons() {
  const res = await getCouponList({ page: 1, size: 20 })
  this.allCoupons[0] = res.data.records
}
```

## 数据库与小程序数据一致性

### 当前状态
✅ **菜单数据**: 已集成，从数据库读取69个菜品
✅ **后端API**: 已创建 `/dish/grouped` 接口
✅ **小程序页面**: 已创建 `menu-new.vue` 使用API

### 待集成
⏳ **活动数据**: 需要更新 `activity-center.vue` 调用API
⏳ **优惠券数据**: 需要更新 `coupon-center.vue` 调用API
⏳ **秒杀数据**: 需要创建秒杀API和更新页面
⏳ **会员数据**: 需要创建会员API和更新页面

## 测试步骤

1. **启动后端服务**
   ```bash
   cd restaurant-server
   mvn spring-boot:run
   ```

2. **确认后端运行**
   访问: http://localhost:8080/dish/grouped
   应该返回菜品数据

3. **启动小程序**
   - 使用HBuilderX打开项目
   - 运行到微信开发者工具
   - 打开菜单页面查看数据

4. **检查数据一致性**
   - 在管理后台修改菜品
   - 刷新小程序菜单页面
   - 确认数据已更新

## 常见问题

### 1. 跨域问题
小程序不存在跨域问题，但H5版本需要后端配置CORS（已配置）

### 2. 请求失败
- 检查后端服务是否启动
- 检查BASE_URL配置是否正确
- 查看控制台错误信息

### 3. 数据不显示
- 检查数据库是否有数据
- 检查API返回格式是否正确
- 查看小程序控制台日志

## 下一步计划

1. ✅ 创建菜单API集成
2. ⏳ 集成活动中心API
3. ⏳ 集成优惠券API
4. ⏳ 集成秒杀API
5. ⏳ 集成会员系统API
6. ⏳ 集成订单系统API

## 注意事项

1. **生产环境配置**: 上线前必须修改BASE_URL为生产服务器地址
2. **域名白名单**: 在微信公众平台配置服务器域名
3. **HTTPS要求**: 生产环境必须使用HTTPS
4. **数据缓存**: 考虑添加本地缓存提升性能
5. **错误处理**: 完善网络错误和数据异常处理
