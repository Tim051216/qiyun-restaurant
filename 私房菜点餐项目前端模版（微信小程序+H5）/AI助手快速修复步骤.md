# AI 助手快速修复步骤

## 🚨 问题原因

你的错误是：**云函数未上传到 uniCloud**

## ⚡ 5 步快速修复

### 步骤 1：创建/关联 uniCloud 服务空间（2分钟）

1. 在 HBuilderX 中，右键点击 `uniCloud-aliyun` 目录
2. 选择 **"关联云服务空间"**
3. 如果没有服务空间，点击 **"创建新空间"**
   - 名称：`qiyun-restaurant`
   - 选择：阿里云
   - 套餐：免费版
4. 等待关联完成

### 步骤 2：上传云函数（3分钟）

1. 在 HBuilderX 中，右键点击 `uniCloud-aliyun/cloudfunctions/ai-chat` 目录
2. 选择 **"上传部署"**
3. 选择 **"上传并运行"**
4. 等待上传完成（首次会安装依赖，需要2-3分钟）

**看到这个提示说明成功：**
```
云函数 ai-chat 上传成功
```

### 步骤 3：验证云函数（1分钟）

1. 右键点击 `ai-chat` 云函数
2. 选择 **"运行-本地云函数"**
3. 输入测试数据：
```json
{
  "message": "你好",
  "history": []
}
```
4. 点击运行，应该看到 AI 回复

### 步骤 4：清除缓存并重启（1分钟）

1. 在微信开发者工具中：
   - 点击 **"清缓存"** → **"清除全部缓存"**
   - 关闭微信开发者工具

2. 在 HBuilderX 中：
   - 点击 **"运行"** → **"停止运行"**
   - 再次点击 **"运行"** → **"运行到小程序模拟器"**

### 步骤 5：测试 AI 助手（1分钟）

1. 在小程序中点击右下角的 🤖 按钮
2. 输入 "你好" 或点击快捷问题
3. 应该能看到 AI 正常回复

## ✅ 成功标志

如果看到以下情况，说明修复成功：

- ✅ AI 助手窗口正常打开
- ✅ 发送消息后有回复
- ✅ 控制台无错误信息
- ✅ 快捷问题可以点击

## ❌ 如果还是失败

### 检查 1：云函数是否真的上传成功

1. 打开 [uniCloud 控制台](https://unicloud.dcloud.net.cn/)
2. 选择你的服务空间
3. 点击 "云函数/云对象"
4. 查看是否有 `ai-chat` 云函数
5. 状态应该是 "运行中"

### 检查 2：API Key 是否正确

打开 `uniCloud-aliyun/cloudfunctions/ai-chat/index.js`，确认：

```javascript
const API_KEY = 'sk-7daca2a765c440c39ada2566908488eb';
```

这个 Key 应该是你的通义千问 API Key。

### 检查 3：查看详细错误

在微信开发者工具控制台运行：

```javascript
// 测试云函数
uniCloud.callFunction({
  name: 'ai-chat',
  data: { message: '测试', history: [] },
  success: (res) => console.log('成功:', res),
  fail: (err) => console.error('失败:', err)
})
```

查看具体错误信息。

## 🆘 常见错误速查

| 错误信息 | 原因 | 解决方案 |
|---------|------|---------|
| `FC invoke failed, resource exhausted` | 云函数未上传 | 执行步骤 2 |
| `uniCloud is not initialized` | 服务空间未关联 | 执行步骤 1 |
| `Invalid API Key` | API Key 错误 | 检查 index.js 中的 API_KEY |
| `timeout` | 网络超时 | 检查网络连接 |
| `云函数不存在` | 云函数未上传 | 执行步骤 2 |

## 📱 联系方式

如果以上步骤都无法解决，请：

1. 截图错误信息
2. 查看云函数日志（uniCloud 控制台）
3. 查看小程序控制台日志
4. 在 [DCloud 社区](https://ask.dcloud.net.cn/) 求助

---

**预计修复时间：** 8-10 分钟  
**难度：** ⭐⭐☆☆☆（简单）
