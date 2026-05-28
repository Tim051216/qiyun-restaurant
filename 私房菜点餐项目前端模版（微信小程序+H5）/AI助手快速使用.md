# AI 助手快速使用指南

## ✅ 你需要做的 3 件事：

### 1️⃣ 获取通义千问 API Key（5分钟）

1. 打开浏览器访问：https://dashscope.console.aliyun.com/
2. 用支付宝扫码登录（或注册阿里云账号）
3. 点击"开通 DashScope"（免费）
4. 进入"API-KEY 管理"
5. 点击"创建新的 API-KEY"
6. 复制生成的 API Key（格式：sk-xxxxxx）

💡 **免费额度**：每月 100 万 tokens，足够个人项目使用！

### 2️⃣ 配置 API Key（1分钟）

打开文件：`api/ai.js`

找到第 9 行，替换 API Key：

```javascript
const API_KEY = 'sk-xxxxxxxxxxxxxxxxxxxxxxxx' // 粘贴你的 API Key
```

### 3️⃣ 配置小程序域名（2分钟）

1. 登录微信小程序后台：https://mp.weixin.qq.com/
2. 左侧菜单：开发 -> 开发管理 -> 开发设置
3. 找到"服务器域名" -> request合法域名
4. 点击"修改"，添加：`https://dashscope.aliyuncs.com`
5. 保存

## 🎉 开始使用

### 在首页添加 AI 助手

打开 `pages/index/index.vue`，添加以下代码：

```vue
<template>
  <view class="container">
    <!-- 你的原有内容 -->
    
    <!-- 添加 AI 助手 -->
    <ai-assistant></ai-assistant>
  </view>
</template>

<script>
// 引入 AI 助手组件
import AiAssistant from '@/components/ai-assistant/ai-assistant.vue'

export default {
  components: {
    AiAssistant  // 注册组件
  },
  // 你的原有代码...
}
</script>
```

保存后，运行小程序，你会在右下角看到一个紫色的 AI 助手按钮！

## 🎨 效果预览

- 右下角会出现一个紫色的悬浮按钮（带动画）
- 点击按钮打开聊天窗口
- 可以输入问题，AI 会智能回复
- 支持快捷问题一键发送

## 🔧 自定义（可选）

### 修改 AI 角色

打开 `api/ai.js`，找到第 23 行：

```javascript
content: '你是七云菜馆的智能点餐助手...'
```

改成你想要的角色设定，比如：
```javascript
content: '你是一个幽默风趣的美食专家，擅长推荐美食...'
```

### 修改快捷问题

打开 `components/ai-assistant/ai-assistant.vue`，找到第 95 行：

```javascript
quickQuestions: [
  '推荐几道招牌菜',
  '有什么辣的菜品？',
  '营业时间是几点？',
  '可以配送吗？'
]
```

改成你想要的快捷问题。

### 修改悬浮按钮位置

在 `components/ai-assistant/ai-assistant.vue` 的样式中：

```scss
.ai-float-btn {
  right: 30rpx;   // 距离右边的距离
  bottom: 150rpx; // 距离底部的距离
}
```

## ⚠️ 注意事项

1. **API Key 安全**：不要把 API Key 提交到 GitHub 等公开平台
2. **域名配置**：必须在小程序后台配置域名，否则无法请求
3. **免费额度**：注意查看使用量，避免超出免费额度

## 🐛 遇到问题？

### 问题1：点击按钮没反应
- 检查是否正确引入组件
- 检查控制台是否有报错

### 问题2：请求失败
- 检查 API Key 是否正确
- 检查小程序域名是否配置
- 检查网络连接

### 问题3：AI 回复很慢
- 这是正常的，AI 需要思考时间
- 可以在 `api/ai.js` 中将模型改为 `qwen-turbo`（更快）

## 📚 更多功能

查看完整文档：`AI助手集成指南.md`

包含：
- 高级配置
- 生产环境部署
- 功能扩展建议
- 常见问题解答

## 🎯 下一步

1. 测试 AI 助手功能
2. 根据你的餐厅信息自定义 AI 角色
3. 添加更多快捷问题
4. 考虑在其他页面也添加 AI 助手

祝你使用愉快！🎉
