# 本地存储工具使用指南

## 概述

c

## 基础功能

### 1. 保存数据

```javascript
import { saveToLocalStorage } from '@/utils/localStorage.js'

// 保存字符串
saveToLocalStorage('username', 'zhangsan')

// 保存数字
saveToLocalStorage('age', 25)

// 保存对象
saveToLocalStorage('userInfo', {
  name: 'zhangsan',
  age: 25,
  phone: '13800138000'
})

// 保存数组
saveToLocalStorage('cart', [
  { id: 1, name: '商品1', price: 10 },
  { id: 2, name: '商品2', price: 20 }
])
```

### 2. 读取数据

```javascript
import { getFromLocalStorage } from '@/utils/localStorage.js'

// 读取数据
const username = getFromLocalStorage('username')

// 读取数据并提供默认值
const age = getFromLocalStorage('age', 18)
const userInfo = getFromLocalStorage('userInfo', {})
const cart = getFromLocalStorage('cart', [])
```

### 3. 删除数据

```javascript
import { removeFromLocalStorage } from '@/utils/localStorage.js'

// 删除单个数据
removeFromLocalStorage('username')
```

### 4. 清空所有数据

```javascript
import { clearLocalStorage } from '@/utils/localStorage.js'

// 清空所有本地存储
clearLocalStorage()
```

### 5. 检查键是否存在

```javascript
import { hasKey } from '@/utils/localStorage.js'

if (hasKey('username')) {
  console.log('用户名已存在')
} else {
  console.log('用户名不存在')
}
```

## 批量操作

### 批量保存

```javascript
import { batchSave } from '@/utils/localStorage.js'

batchSave({
  username: 'zhangsan',
  age: 25,
  phone: '13800138000'
})
```

### 批量读取

```javascript
import { batchGet } from '@/utils/localStorage.js'

const data = batchGet(['username', 'age', 'phone'])
console.log(data)
// { username: 'zhangsan', age: 25, phone: '13800138000' }
```

### 批量删除

```javascript
import { batchRemove } from '@/utils/localStorage.js'

batchRemove(['username', 'age', 'phone'])
```

## 存储信息

### 获取所有键名

```javascript
import { getAllKeys } from '@/utils/localStorage.js'

const keys = getAllKeys()
console.log('所有键名:', keys)
```

### 获取存储信息

```javascript
import { getStorageInfo } from '@/utils/localStorage.js'

const info = getStorageInfo()
console.log('键名列表:', info.keys)
console.log('当前大小:', info.currentSize)
console.log('限制大小:', info.limitSize)
```

## 业务场景封装

### 桌号存储 (TableStorage)

#### 保存桌号信息

```javascript
import { TableStorage } from '@/utils/localStorage.js'

// 保存桌号和人数
TableStorage.save('A01', 4)
```

#### 获取桌号信息

```javascript
const tableInfo = TableStorage.get()
console.log('桌号:', tableInfo.tableNumber)
console.log('人数:', tableInfo.dinerCount)
console.log('扫码时间:', tableInfo.scanTime)
```

#### 检查是否有桌号

```javascript
if (TableStorage.has()) {
  console.log('已有桌号信息')
} else {
  console.log('没有桌号信息')
}
```

#### 清除桌号信息

```javascript
TableStorage.clear()
```

### 购物车存储 (CartStorage)

#### 保存购物车

```javascript
import { CartStorage } from '@/utils/localStorage.js'

const cart = [
  { id: 1, name: '宫保鸡丁', price: 38, count: 2 },
  { id: 2, name: '麻婆豆腐', price: 28, count: 1 }
]

CartStorage.save(cart)
```

#### 获取购物车

```javascript
const cart = CartStorage.get()
console.log('购物车:', cart)
```

#### 添加商品到购物车

```javascript
// 添加新商品
CartStorage.addItem({
  id: 3,
  name: '鱼香肉丝',
  price: 32,
  count: 1
})

// 如果商品已存在，会自动增加数量
CartStorage.addItem({
  id: 1,
  name: '宫保鸡丁',
  price: 38,
  count: 1  // 会在原有基础上 +1
})
```

#### 移除购物车商品

```javascript
// 移除指定商品
CartStorage.removeItem(1)
```

#### 更新商品数量

```javascript
// 更新商品数量
CartStorage.updateItemCount(1, 3)

// 数量为0时会自动移除
CartStorage.updateItemCount(1, 0)
```

#### 清空购物车

```javascript
CartStorage.clear()
```

### 用户信息存储 (UserStorage)

#### 保存用户信息

```javascript
import { UserStorage } from '@/utils/localStorage.js'

UserStorage.save({
  id: 123,
  name: '张三',
  phone: '13800138000',
  avatar: 'https://example.com/avatar.jpg'
})
```

#### 获取用户信息

```javascript
const userInfo = UserStorage.get()
console.log('用户信息:', userInfo)
```

#### 检查是否已登录

```javascript
if (UserStorage.isLoggedIn()) {
  console.log('用户已登录')
} else {
  console.log('用户未登录')
}
```

#### 清除用户信息（退出登录）

```javascript
UserStorage.clear()
```

## 存储键名常量

使用预定义的键名常量，避免拼写错误：

```javascript
import { STORAGE_KEYS } from '@/utils/localStorage.js'

console.log(STORAGE_KEYS.TABLE_NUMBER)    // 'tableNumber'
console.log(STORAGE_KEYS.DINER_COUNT)     // 'dinerCount'
console.log(STORAGE_KEYS.SCAN_TIME)       // 'scanTime'
console.log(STORAGE_KEYS.USER_INFO)       // 'userInfo'
console.log(STORAGE_KEYS.CART)            // 'cart'
console.log(STORAGE_KEYS.ORDER_HISTORY)   // 'orderHistory'
```

## 完整示例

### 示例 1: 购物车管理

```javascript
import { CartStorage } from '@/utils/localStorage.js'

export default {
  data() {
    return {
      cart: []
    }
  },
  onLoad() {
    // 加载购物车
    this.cart = CartStorage.get()
  },
  methods: {
    // 添加商品
    addToCart(dish) {
      CartStorage.addItem({
        id: dish.id,
        name: dish.name,
        price: dish.price,
        count: 1
      })
      
      // 更新本地数据
      this.cart = CartStorage.get()
    },
    
    // 更新数量
    updateCount(dishId, count) {
      CartStorage.updateItemCount(dishId, count)
      this.cart = CartStorage.get()
    },
    
    // 清空购物车
    clearCart() {
      CartStorage.clear()
      this.cart = []
    }
  }
}
```

### 示例 2: 用户登录状态管理

```javascript
import { UserStorage } from '@/utils/localStorage.js'

export default {
  data() {
    return {
      isLoggedIn: false,
      userInfo: null
    }
  },
  onLoad() {
    // 检查登录状态
    this.isLoggedIn = UserStorage.isLoggedIn()
    if (this.isLoggedIn) {
      this.userInfo = UserStorage.get()
    }
  },
  methods: {
    // 登录
    async login(username, password) {
      // 调用登录接口
      const res = await loginApi(username, password)
      
      if (res.code === 1) {
        // 保存用户信息
        UserStorage.save(res.data)
        this.isLoggedIn = true
        this.userInfo = res.data
      }
    },
    
    // 退出登录
    logout() {
      UserStorage.clear()
      this.isLoggedIn = false
      this.userInfo = null
    }
  }
}
```

### 示例 3: 桌号管理

```javascript
import { TableStorage } from '@/utils/localStorage.js'

export default {
  data() {
    return {
      tableNumber: '',
      dinerCount: 0,
      showTableDialog: false
    }
  },
  onLoad(options) {
    // 检查是否有桌号信息
    if (TableStorage.has()) {
      const tableInfo = TableStorage.ions.scene) {
      // 从扫码进入
      this.tableNumber = decodeURIComponent(options.scene)
      this.showTableDialog = true
    }
  },
  methods: {
    // 确认桌号
    confirmTable() {
      TableStorage.save(this.tableNumber, this.dinerCount)
      this.showTableDialog = false
    },
    
    // 清除桌号（离店）
    leaveTable() {
      TableStorage.clear()
      this.tableNumber = ''
      this.dinerCount = 0
    }
  }
}
```

## 错误处理

所有存储操作都包含错误处理，会在控制台输出详细日志：

```javascript
import { saveToLocalStorage } from '@/utils/localStorage.js'

const success 