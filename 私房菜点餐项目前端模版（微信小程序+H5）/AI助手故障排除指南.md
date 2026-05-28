# AI 助手故障排除指南

## 🔴 当前问题分析

根据错误日志，你遇到了以下问题：

### 错误 1：云函数调用失败
```
Error: [ai-chat]: FC invoke failed, resource exhausted.
```

**原因：** uniCloud 服务空间未正确配置或云函数未上传

### 错误 2：网络未连接
```
网络连接失败，请稍后重试
```

**原因：** 云函数调用失败导致的连锁反应

## ✅ 完整解决方案

### 第一步：创建 uniCloud 服务空间

#### 1.1 在 HBuilderX 中创建服务空间

1. 打开 HBuilderX
2. 打开你的项目
3. 右键点击项目根目录
4. 选择 "创建 uniCloud 云开发环境"
5. 选择 "阿里云"
6. 登录 DCloud 账号
7. 创建新的服务空间（或选择已有的）
   - 服务空间名称：`qiyun-restaurant`（或其他名称）
   - 选择免费版即可

#### 1.2 关联服务空间

1. 右键点击 `uniCloud-aliyun` 目录
2. 选择 "关联云服务空间"
3. 选择刚才创建的服务空间
4. 等待关联完成

### 第二步：上传云函数

#### 2.1 检查云函数文件

确保以下文件存在：
- `uniCloud-aliyun/cloudfunctions/ai-chat/index.js`
- `uniCloud-aliyun/cloudfunctions/ai-chat/package.json`

#### 2.2 上传云函数

1. 在 HBuilderX 中，右键点击 `uniCloud-aliyun/cloudfunctions/ai-chat` 目录
2. 选择 "上传部署"
3. 选择 "上传并运行"
4. 等待上传完成（可能需要几分钟）

**重要提示：** 
- 首次上传会自动安装依赖包
- 如果上传失败，检查网络连接
- 确保 DCloud 账号已登录

### 第三步：配置云函数权限

#### 3.1 在 uniCloud 控制台配置

1. 打开 [uniCloud 控制台](https://unicloud.dcloud.net.cn/)
2. 选择你的服务空间
3. 点击左侧 "云函数/云对象"
4. 找到 `ai-chat` 云函数
5. 点击 "详情"
6. 确认状态为 "运行中"

#### 3.2 测试云函数

在 HBuilderX 中：
1. 右键点击 `ai-chat` 云函数
2. 选择 "运行-本地云函数"
3. 输入测试参数：
```json
{
  "message": "你好",
  "history": []
}
```
4. 点击运行，查看返回结果

### 第四步：配置小程序

#### 4.1 检查 manifest.json

1. 打开 `manifest.json`
2. 找到 "mp-weixin" 配置
3. 确保 uniCloud 配置正确：

```json
{
  "mp-weixin": {
    "appid": "你的小程序AppID",
    "uniStatistics": {
      "enable": false
    },
    "usingComponents": true
  },
  "uniCloud": {
    "provider": "aliyun",
    "spaceId": "你的服务空间ID"
  }
}
```

#### 4.2 初始化 uniCloud

在 `main.js` 中确保已初始化（通常 uni-app 会自动处理）：

```javascript
// 如果没有，添加以下代码
import Vue from 'vue'
import App from './App'

Vue.config.productionTip = false

App.mpType = 'app'

const app = new Vue({
  ...App
})
app.$mount()
```

### 第五步：重新运行小程序

#### 5.1 清除缓存

1. 在微信开发者工具中，点击 "清缓存" → "清除全部缓存"
2. 关闭微信开发者工具

#### 5.2 重新编译

1. 在 HBuilderX 中，点击 "运行" → "停止运行"
2. 再次点击 "运行" → "运行到小程序模拟器" → "微信开发者工具"
3. 等待编译完成

#### 5.3 测试 AI 助手

1. 在小程序中点击右下角的 AI 助手按钮
2. 输入 "你好" 测试
3. 查看是否正常回复

## 🔍 验证清单

完成以上步骤后，请逐一验证：

- [ ] uniCloud 服务空间已创建并关联
- [ ] 云函数 `ai-chat` 已上传成功
- [ ] 云函数状态为 "运行中"
- [ ] 云函数测试返回正常
- [ ] manifest.json 配置正确
- [ ] 小程序已重新编译
- [ ] AI 助手按钮可以点击
- [ ] 聊天窗口可以正常显示
- [ ] 发送消息后有正常回复

## ❌ 常见错误及解决方案

### 错误 1：云函数上传失败

**错误信息：**
```
上传失败：网络错误
```

**解决方案：**
1. 检查网络连接
2. 检查 DCloud 账号是否登录
3. 尝试切换网络（如使用手机热点）
4. 重启 HBuilderX

### 错误 2：云函数运行超时

**错误信息：**
```
FC invoke failed, timeout
```

**解决方案：**
1. 检查通义千问 API Key 是否正确
2. 检查网络是否能访问阿里云 API
3. 增加云函数超时时间（在控制台配置）

### 错误 3：API Key 无效

**错误信息：**
```
Invalid API Key
```

**解决方案：**
1. 检查 `ai-chat/index.js` 中的 API_KEY
2. 确认 API Key 格式：`sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`
3. 登录阿里云控制台重新生成 API Key
4. 重新上传云函数

### 错误 4：免费额度用完

**错误信息：**
```
resource exhausted
```

**解决方案：**
1. 登录阿里云控制台查看用量
2. 等待下月额度刷新
3. 或购买付费套餐

### 错误 5：服务空间未关联

**错误信息：**
```
uniCloud is not initialized
```

**解决方案：**
1. 右键点击 `uniCloud-aliyun` 目录
2. 选择 "关联云服务空间"
3. 选择正确的服务空间
4. 重新运行小程序

## 🛠️ 调试技巧

### 1. 查看云函数日志

在 uniCloud 控制台：
1. 进入 "云函数/云对象"
2. 选择 `ai-chat` 云函数
3. 点击 "日志"
4. 查看最近的调用记录和错误信息

### 2. 查看小程序控制台

在微信开发者工具中：
1. 打开 "调试器" 面板
2. 查看 "Console" 标签
3. 查找错误信息和日志

### 3. 使用 console.log 调试

在 `api/ai.js` 中添加更多日志：

```javascript
export function chatWithAI(message, history = []) {
	console.log('[AI] 开始调用，消息:', message)
	console.log('[AI] 历史记录:', history)
	
	return new Promise((resolve, reject) => {
		uniCloud.callFunction({
			name: 'ai-chat',
			data: {
				message: message,
				history: history
			},
			success: (res) => {
				console.log('[AI] 调用成功，完整响应:', res)
				console.log('[AI] 结果数据:', res.result)
				// ... 其他代码
			},
			fail: (err) => {
				console.error('[AI] 调用失败，完整错误:', err)
				console.error('[AI] 错误代码:', err.errCode)
				console.error('[AI] 错误信息:', err.errMsg)
				// ... 其他代码
			}
		})
	})
}
```

## 📞 获取帮助

如果以上方法都无法解决问题，请：

1. **查看官方文档**
   - [uniCloud 文档](https://uniapp.dcloud.net.cn/uniCloud/)
   - [通义千问文档](https://help.aliyun.com/zh/dashscope/)

2. **检查错误日志**
   - 云函数日志（uniCloud 控制台）
   - 小程序控制台日志
   - HBuilderX 控制台日志

3. **社区求助**
   - [DCloud 社区](https://ask.dcloud.net.cn/)
   - [阿里云开发者社区](https://developer.aliyun.com/)

## 🎯 快速检查命令

在微信开发者工具控制台中运行：

```javascript
// 检查 uniCloud 是否初始化
console.log('uniCloud:', uniCloud)

// 测试云函数调用
uniCloud.callFunction({
  name: 'ai-chat',
  data: {
    message: '测试',
    history: []
  },
  success: (res) => {
    console.log('测试成功:', res)
  },
  fail: (err) => {
    console.error('测试失败:', err)
  }
})
```

## ✨ 成功标志

当你看到以下情况时，说明配置成功：

1. ✅ 云函数日志显示正常调用
2. ✅ 小程序控制台无错误信息
3. ✅ AI 助手可以正常回复消息
4. ✅ 对话历史正常保存
5. ✅ 快捷问题可以正常使用

---

**最后更新：** 2026-02-20  
**适用版本：** uni-app + uniCloud + 阿里云通义千问
