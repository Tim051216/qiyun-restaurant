# 启动参数处理工具使用指南

## 概述

`launchParams.js` 提供了统一的启动参数处理功能，支持微信小程序和 H5 两种环境。

## 主要功能

### 1. handleLaunchParams(options)

统一处理启动参数，自动识别环境并获取参数。

**参数:**
- `options` (Object): onLoad 的 options 参数（小程序环境必传）

**返回:**
- Object: 启动参数对象

**示例:**
```javascript
import { handleLaunchParams } from '@/utils/launchParams.js'

export default {
  onLoad(options) {
    const params = handleLaunchParams(options)
    console.log('启动参数:', params)
    // 输出: { table: 'A01', dinerCount: '4' }
  }
}
```

### 2. getTableInfo(options)

获取桌号信息，优先从启动参数获取，其次从本地存储获取。

**参数:**
- `options` (Object): onLoad 的 options 参数

**返回:**
- Object: 桌号信息对象
  - `tableNumber` (String): 桌号
  - `dinerCount` (Number): 就餐人数
  - `scanTime` (Number): 扫码时间戳
  - `fromScan` (Boolean): 是否来自扫码

**示例:**
```javascript
import { getTableInfo } from '@/utils/launchParams.js'

export default {
  onLoad(options) {
    const tableInfo = getTableInfo(options)
    
    if (tableInfo.tableNumber) {
      console.log('桌号:', tableInfo.tableNumber)
      console.log('人数:', tableInfo.dinerCount)
      console.log('来自扫码:', tableInfo.fromScan)
    } else {
      console.log('没有桌号信息')
    }
  }
}
```

### 3. saveTableInfo(tableNumber, dinerCount)

保存桌号信息到本地存储。

**参数:**
- `tableNumber` (String): 桌号
- `dinerCount` (Number): 就餐人数

**返回:**
- Boolean: 是否保存成功

**示例:**
```javascript
import { saveTableInfo } from '@/utils/launchParams.js'

const success = saveTableInfo('A01', 4)
if (success) {
  console.log('保存成功')
}
```

### 4. clearTableInfo()

清除本地存储的桌号信息。

**返回:**
- Boolean: 是否清除成功

**示例:**
```javascript
import { clearTableInfo } from '@/utils/launchParams.js'

const success = clearTableInfo()
if (success) {
  console.log('清除成功')
}
```

### 5. parseSceneParam(scene)

解析小程序码的 scene 参数。

**参数:**
- `scene` (String): 场景值参数

**返回:**
- Object: 解析后的参数对象

**支持格式:**
- `table=A01` → `{ table: 'A01' }`
- `table=A01&dinerCount=4` → `{ table: 'A01', dinerCount: '4' }`
- `A01` → `{ table: 'A01' }`

**示例:**
```javascript
import { parseSceneParam } from '@/utils/launchParams.js'

const params = parseSceneParam('table=A01&dinerCount=4')
console.log(params) // { table: 'A01', dinerCount: '4' }
```

### 6. detectEnvironment()

检测当前运行环境。

**返回:**
- String: 'mp-weixin' | 'h5' | 'unknown'

**示例:**
```javascript
import { detectEnvironment } from '@/utils/launchParams.js'

const env = detectEnvironment()
console.log('当前环境:', env)
```

## 使用场景

### 场景 1: 首页处理扫码进入

```javascript
import { getTableInfo, saveTableInfo } from '@/utils/launchParams.js'

export default {
  data() {
    return {
      tableNumber: '',
      showTableDialog: false,
      dinerCount: 1
    }
  },
  onLoad(options) {
    const tableInfo = getTableInfo(options)
    
    if (tableInfo.tableNumber && tableInfo.fromScan) {
      // 从扫码进入
      this.tableNumber = tableInfo.tableNumber
      this.dinerCount = tableInfo.dinerCount || 1
      this.showTableDialog = true
    }
  },
  methods: {
    confirmTable() {
      // 保存桌号信息
      saveTableInfo(this.tableNumber, this.dinerCount)
      this.showTableDialog = false
    }
  }
}
```

### 场景 2: 点餐页面获取桌号

```javascript
import { getTableInfo } from '@/utils/launchParams.js'

export default {
  data() {
    return {
      tableNo: ''
    }
  },
  onLoad(options) {
    const tableInfo = getTableInfo(options)
    
    if (tableInfo.tableNumber) {
      this.tableNo = tableInfo.tableNumber
    } else {
      // 没有桌号，跳转回首页
      uni.showToast({
        title: '请先扫码获取桌号',
        icon: 'none'
      })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 1500)
    }
  }
}
```

### 场景 3: H5 环境 URL 参数传递

H5 环境下，可以通过 URL 传递参数：

```
https://your-domain.com/pages/index/index?table=A01&dinerCount=4
```

工具会自动解析这些参数。

## 环境差异处理

工具内部使用条件编译处理不同环境：

```javascript
// 小程序环境
// #ifdef MP-WEIXIN
return 'mp-weixin'
// #endif

// H5 环境
// #ifdef H5
return 'h5'
// #endif
```

## 错误处理

所有函数都包含错误处理，会在控制台输出详细的错误信息：

```javascript
try {
  const params = handleLaunchParams(options)
} catch (error) {
  console.error('[LaunchParams] 处理失败:', error)
  uni.showToast({
    title: '参数解析失败',
    icon: 'none'
  })
}
```

## 测试

运行测试文件验证功能：

```javascript
import { runTests } from '@/utils/launchParams.test.js'

runTests()
```

## 注意事项

1. **小程序环境**: 必须传递 `options` 参数给 `handleLaunchParams()`
2. **H5 环境**: 会自动从 URL 获取参数，不需要传递 `options`
3. **本地存储**: 桌号信息会保存在本地，下次打开应用时可以恢复
4. **参数格式**: scene 参数支持多种格式，工具会自动识别和解析
5. **错误处理**: 所有函数都有完善的错误处理，不会导致应用崩溃

## 更新日志

### v1.0.0 (2026-02-09)
- 初始版本
- 支持小程序和 H5 环境
- 提供统一的启动参数处理接口
- 支持本地存储管理
- 完善的错误处理和日志记录
