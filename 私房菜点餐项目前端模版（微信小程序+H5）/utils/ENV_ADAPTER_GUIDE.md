# 环境适配器使用指南

## 概述

`envAdapter.js` 提供了跨平台的 API 适配功能，统一了微信小程序、H5 和 APP 等不同环境的 API 差异。

## 核心功能

### 1. 环境检测

#### detectEnvironment()

检测当前运行环境。

**返回值:**
- `'mp-weixin'`: 微信小程序
- `'h5'`: H5 网页
- `'app'`: APP
- `'unknown'`: 未知环境

**示例:**
```javascript
import { detectEnvironment } from '@/utils/envAdapter.js'

const env = detectEnvironment()
console.log('当前环境:', env)

if (env === 'mp-weixin') {
  console.log('运行在微信小程序中')
} else if (env === 'h5') {
  console.log('运行在 H5 中')
}
```

#### getEnvironmentInfo()

获取详细的环境信息。

**返回值:**
```javascript
{
  platform: 'mp-weixin',
  isWeixin: true,
  isH5: false,
  isApp: false,
  systemInfo: { ... }  // uni.getSystemInfoSync() 的结果
}
```

**示例:**
```javascript
import { getEnvironmentInfo } from '@/utils/envAdapter.js'

const info = getEnvironmentInfo()
console.log('平台:', info.platform)
console.log('系统信息:', info.systemInfo)
```

### 2. 存储适配器 (StorageAdapter)

提供统一的本地存储接口，自动适配不同环境。

#### set(key, value)

设置存储。

**示例:**
```javascript
import { StorageAdapter } from '@/utils/envAdapter.js'

// 存储字符串
StorageAdapter.set('username', 'zhangsan')

// 存储对象
StorageAdapter.set('userInfo', {
  name: 'zhangsan',
  age: 25
})

// 存储数组
StorageAdapter.set('cart', [1, 2, 3])
```

#### get(key)

获取存储。

**示例:**
```javascript
const username = StorageAdapter.get('username')
const userInfo = StorageAdapter.get('userInfo')
const cart = StorageAdapter.get('cart')
```

#### remove(key)

删除存储。

**示例:**
```javascript
StorageAdapter.remove('username')
```

#### clear()

清空所有存储。

**示例:**
```javascript
StorageAdapter.clear()
```

### 3. 导航适配器 (NavigationAdapter)

提供统一的页面导航接口。

#### navigateTo(url, options)

跳转到新页面。

**示例:**
```javascript
import { NavigationAdapter } from '@/utils/envAdapter.js'

// 基本跳转
NavigationAdapter.navigateTo('/pages/detail/detail?id=123')

// 带选项跳转
NavigationAdapter.navigateTo('/pages/detail/detail', {
  success: () => console.log('跳转成功'),
  fail: () => console.log('跳转失败')
})
```

#### switchTab(url)

跳转到 tabBar 页面。

**示例:**
```javascript
NavigationAdapter.switchTab('/pages/index/index')
```

#### navigateBack(delta)

返回上一页。

**示例:**
```javascript
// 返回上一页
NavigationAdapter.navigateBack()

// 返回上两页
NavigationAdapter.navigateBack(2)
```

#### redirectTo(url)

重定向到页面（关闭当前页面）。

**示例:**
```javascript
NavigationAdapter.redirectTo('/pages/login/login')
```

#### reLaunch(url)

重新加载应用（关闭所有页面）。

**示例:**
```javascript
NavigationAdapter.reLaunch('/pages/index/index')
```

### 4. 网络请求适配器 (RequestAdapter)

提供统一的网络请求接口。

#### request(config)

发起网络请求。

**示例:**
```javascript
import { RequestAdapter } from '@/utils/envAdapter.js'

// GET 请求
const res = await RequestAdapter.request({
  url: 'https://api.example.com/data',
  method: 'GET'
})

// POST 请求
const res = await RequestAdapter.request({
  url: 'https://api.example.com/submit',
  method: 'POST',
  data: {
    name: 'zhangsan',
    age: 25
  }
})
```

#### uploadFile(config)

上传文件。

**示例:**
```javascript
const res = await RequestAdapter.uploadFile({
  url: 'https://api.example.com/upload',
  filePath: tempFilePath,
  name: 'file'
})
```

#### downloadFile(config)

下载文件。

**示例:**
```javascript
const res = await RequestAdapter.downloadFile({
  url: 'https://example.com/file.pdf'
})
```

### 5. 扫码适配器 (ScanAdapter)

提供统一的扫码接口。

#### scanCode(options)

扫描二维码/条形码。

**示例:**
```javascript
import { ScanAdapter } from '@/utils/envAdapter.js'

try {
  const res = await ScanAdapter.scanCode({
    onlyFromCamera: true,
    scanType: ['qrCode', 'barCode']
  })
  
  console.log('扫码结果:', res.result)
} catch (error) {
  console.error('扫码失败:', error)
}
```

**注意:** H5 环境不支持扫码功能。

### 6. 分享适配器 (ShareAdapter)

提供统一的分享接口。

#### shareToWeixin(options)

分享到微信。

**示例:**
```javascript
import { ShareAdapter } from '@/utils/envAdapter.js'

// 在小程序中使用
export default {
  onShareAppMessage() {
    return ShareAdapter.shareToWeixin({
      title: '七云菜馆',
      path: '/pages/index/index?table=A01',
      imageUrl: '/static/share.jpg'
    })
  }
}
```

### 7. 支付适配器 (PaymentAdapter)

提供统一的支付接口。

#### requestPayment(paymentData)

发起支付。

**示例:**
```javascript
import { PaymentAdapter } from '@/utils/envAdapter.js'

try {
  const res = await PaymentAdapter.requestPayment({
    timeStamp: '1234567890',
    nonceStr: 'abc123',
    package: 'prepay_id=xxx',
    signType: 'MD5',
    paySign: 'xxx'
  })
  
  console.log('支付成功')
} catch (error) {
  console.error('支付失败:', error)
}
```

## 高级功能

### getAdapter(type)

根据类型获取适配器。

**参数:**
- `'storage'`: 存储适配器
- `'navigation'`: 导航适配器
- `'request'`: 网络请求适配器
- `'scan'`: 扫码适配器
- `'share'`: 分享适配器
- `'payment'`: 支付适配器

**示例:**
```javascript
import { getAdapter } from '@/utils/envAdapter.js'

const storage = getAdapter('storage')
storage.set('key', 'value')

const navigation = getAdapter('navigation')
navigation.navigateTo('/pages/detail/detail')
```

### isFeatureSupported(feature)

检查功能是否支持。

**参数:**
- `'scan'`: 扫码
- `'payment'`: 支付
- `'share'`: 分享
- `'location'`: 定位
- `'camera'`: 相机
- `'bluetooth'`: 蓝牙

**示例:**
```javascript
import { isFeatureSupported } from '@/utils/envAdapter.js'

if (isFeatureSupported('scan')) {
  console.log('当前环境支持扫码')
} else {
  console.log('当前环境不支持扫码')
}
```

## 完整示例

### 示例 1: 跨平台存储

```javascript
import { StorageAdapter } from '@/utils/envAdapter.js'

export default {
  data() {
    return {
      userInfo: null
    }
  },
  onLoad() {
    // 读取用户信息
    this.userInfo = StorageAdapter.get('userInfo')
  },
  methods: {
    saveUserInfo() {
      // 保存用户信息
      StorageAdapter.set('userInfo', {
        name: 'zhangsan',
        age: 25
      })
    },
    clearUserInfo() {
      // 清除用户信息
      StorageAdapter.remove('userInfo')
    }
  }
}
```

### 示例 2: 条件功能

```javascript
import { isFeatureSupported, ScanAdapter } from '@/utils/envAdapter.js'

export default {
  methods: {
    async handleScan() {
      if (!isFeatureSupported('scan')) {
        uni.showToast({
          title: '当前环境不支持扫码',
          icon: 'none'
        })
        return
      }
      
      try {
        const res = await ScanAdapter.scanCode()
        console.log('扫码结果:', res.result)
      } catch (error) {
        console.error('扫码失败:', error)
      }
    }
  }
}
```

### 示例 3: 环境特定逻辑

```javascript
import { detectEnvironment, getEnvironmentInfo } from '@/utils/envAdapter.js'

export default {
  onLoad() {
    const env = detectEnvironment()
    
    if (env === 'mp-weixin') {
      // 小程序特定逻辑
      this.initMiniProgram()
    } else if (env === 'h5') {
      // H5 特定逻辑
      this.initH5()
    }
  },
  methods: {
    initMiniProgram() {
      console.log('初始化小程序')
    },
    initH5() {
      console.log('初始化 H5')
    }
  }
}
```

## 环境差异说明

### 小程序 vs H5

| 功能 | 小程序 | H5 |
|------|--------|-----|
| 扫码 | ✅ 支持 | ❌ 不支持 |
| 支付 | ✅ 原生支持 | ⚠️ 需要 JS-SDK |
| 分享 | ✅ 原生支持 | ⚠️ 需要 JS-SDK |
| 定位 | ✅ 支持 | ✅ 支持 |
| 相机 | ✅ 支持 | ✅ 支持 |
| 蓝牙 | ✅ 支持 | ❌ 不支持 |

## 最佳实践

1. **优先使用适配器**: 始终使用适配器而不是直接调用 uni API
2. **检查功能支持**: 使用 `isFeatureSupported()` 检查功能是否支持
3. **错误处理**: 所有异步操作都要添加 try-catch
4. **环境判断**: 使用 `detectEnvironment()` 而不是条件编译
5. **统一接口**: 保持代码在不同环境下的一致性

## 注意事项

1. H5 环境的某些功能需要额外配置（如支付、分享需要微信 JS-SDK）
2. 存储适配器会自动处理数据序列化，无需手动 JSON.stringify
3. 网络请求适配器返回 Promise，便于使用 async/await
4. 扫码功能在 H5 环境不可用，需要提前检查

## 更新日志

### v1.0.0 (2026-02-09)
- 初始版本
- 支持小程序、H5、APP 三种环境
- 提供存储、导航、网络、扫码、分享、支付六大适配器
- 完善的功能检测机制
