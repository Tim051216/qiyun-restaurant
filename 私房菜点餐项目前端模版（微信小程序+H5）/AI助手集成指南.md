# AI 助手集成指南 - 通义千问

## 📋 功能说明

已为小程序集成阿里云通义千问 AI 助手，提供以下功能：
- 🤖 智能菜品推荐
- 💬 智能客服问答
- 🍽️ 点餐协助
- ⚡ 快捷问题回复

## 🚀 快速开始

### 1. 获取通义千问 API Key

1. 访问阿里云 DashScope 控制台：https://dashscope.console.aliyun.com/
2. 注册/登录阿里云账号
3. 开通 DashScope 服务（通义千问）
4. 在 API-KEY 管理页面创建新的 API Key
5. 复制 API Key

**免费额度说明：**
- qwen-turbo 模型：每月 100 万 tokens 免费额度
- 足够个人项目和小型应用使用

### 2. 配置 API Key

打开 `api/ai.js` 文件，替换 API Key：

```javascript
const API_KEY = 'sk-xxxxxxxxxxxxxxxxxxxxxxxx' // 替换为你的 API Key
```

⚠️ **安全提示：**
- 生产环境建议通过后端代理调用 AI API
- 不要将 API Key 提交到公开的代码仓库
- 可以使用环境变量或配置文件管理 API Key

### 3. 配置小程序服务器域名

在微信小程序后台配置合法域名：

1. 登录微信小程序后台
2. 进入 开发 -> 开发管理 -> 开发设置 -> 服务器域名
3. 添加 request 合法域名：`https://dashscope.aliyuncs.com`

### 4. 在页面中使用 AI 助手

#### 方式一：全局组件（推荐）

在 `pages/index/index.vue` 中引入：

\`\`\`vue
<template>
  <view class="container">
    <!-- 你的页面内容 -->
    
    <!-- AI 助手组件 -->
    <ai-assistant></ai-assistant>
  </view>
</template>

<script>
import AiAssistant from '@/components/ai-assistant/ai-assistant.vue'

export default {
  components: {
    AiAssistant
  }
}
</script>
\`\`\`

#### 方式二：按需引入

只在需要的页面引入 AI 助手组件。

## 📁 文件说明

### 1. `api/ai.js` - AI API 接口

提供以下方法：

```javascript
// 通用对话
chatWithAI(message, history)

// 菜品推荐
getDishRecommendation(preference)

// 客服问答
askCustomerService(question)
```

### 2. `components/ai-assistant/ai-assistant.vue` - AI 助手组件

功能特性：
- 悬浮按钮入口
- 聊天对话界面
- 快捷问题
- 消息历史记录
- 加载动画
- 流畅的交互体验

## 🎨 自定义配置

### 修改 AI 角色设定

在 `api/ai.js` 中修改 system prompt：

```javascript
{
  role: 'system',
  content: '你是七云菜馆的智能点餐助手...' // 修改这里
}
```

### 修改快捷问题

在 `components/ai-assistant/ai-assistant.vue` 中修改：

```javascript
quickQuestions: [
  '推荐几道招牌菜',
  '有什么辣的菜品？',
  // 添加更多快捷问题
]
```

### 修改样式

组件使用 SCSS 编写，可以自定义：
- 悬浮按钮位置和样式
- 聊天窗口大小和颜色
- 消息气泡样式
- 动画效果

## 🔧 高级配置

### 1. 切换模型

在 `api/ai.js` 中修改模型：

```javascript
data: {
  model: 'qwen-turbo',  // 可选：qwen-turbo, qwen-plus, qwen-max
  // ...
}
```

模型对比：
- `qwen-turbo`：速度快，免费额度多，适合日常对话
- `qwen-plus`：平衡性能和成本
- `qwen-max`：最强性能，适合复杂任务

### 2. 调整参数

```javascript
parameters: {
  result_format: 'message',
  max_tokens: 500,      // 最大输出长度
  temperature: 0.7,     // 创造性（0-1，越高越随机）
  top_p: 0.8           // 采样范围
}
```

### 3. 添加菜品知识库

可以在 system prompt 中添加菜品信息：

```javascript
const systemPrompt = \`你是七云菜馆的AI助手。

菜单信息：
1. 招牌红烧肉 - ¥38 - 肥而不腻，入口即化
2. 清蒸鲈鱼 - ¥58 - 鲜嫩多汁，营养丰富
3. 麻辣香锅 - ¥48 - 香辣过瘾，食材丰富
...

请根据用户需求推荐合适的菜品。\`
```

## 🛡️ 生产环境部署建议

### 方案一：后端代理（推荐）

创建后端 API 代理通义千问：

1. 在后端服务（如 Spring Boot）创建代理接口
2. 后端存储 API Key
3. 小程序调用后端接口
4. 后端转发请求到通义千问

优点：
- API Key 不暴露
- 可以添加访问控制
- 可以记录使用情况
- 可以实现缓存优化

### 方案二：云函数

使用 uniCloud 云函数：

1. 创建云函数
2. 在云函数中调用通义千问 API
3. 小程序调用云函数

## 📊 使用统计

通义千问会返回 token 使用情况：

```javascript
{
  usage: {
    input_tokens: 100,    // 输入 tokens
    output_tokens: 50,    // 输出 tokens
    total_tokens: 150     // 总计 tokens
  }
}
```

可以在组件中添加统计功能，监控使用量。

## 🐛 常见问题

### 1. 请求失败：域名不合法

**解决方案：**
- 检查小程序后台是否配置了 `https://dashscope.aliyuncs.com`
- 确保域名配置已保存并生效

### 2. API Key 无效

**解决方案：**
- 检查 API Key 是否正确复制
- 确认 API Key 前缀是 `sk-`
- 检查 DashScope 服务是否已开通

### 3. 响应速度慢

**解决方案：**
- 使用 qwen-turbo 模型（速度最快）
- 减少 max_tokens 参数
- 考虑添加缓存机制

### 4. 免费额度用完

**解决方案：**
- 查看控制台使用情况
- 优化 prompt 减少 token 消耗
- 考虑购买付费套餐

## 📝 示例代码

### 在菜单页面添加 AI 推荐

```vue
<template>
  <view>
    <button @click="getAIRecommendation">AI 推荐菜品</button>
  </view>
</template>

<script>
import { getDishRecommendation } from '@/api/ai.js'

export default {
  methods: {
    async getAIRecommendation() {
      uni.showLoading({ title: '正在推荐...' })
      try {
        const res = await getDishRecommendation('清淡健康')
        uni.hideLoading()
        uni.showModal({
          title: 'AI 推荐',
          content: res.message,
          showCancel: false
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '推荐失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>
```

## 🎯 功能扩展建议

1. **语音输入**：集成语音识别，支持语音点餐
2. **图片识别**：上传菜品图片，AI 识别并推荐
3. **个性化推荐**：根据用户历史订单推荐
4. **多轮对话**：支持复杂的对话流程
5. **情感分析**：分析用户情绪，提供更好的服务

## 📞 技术支持

- 通义千问文档：https://help.aliyun.com/zh/dashscope/
- API 参考：https://help.aliyun.com/zh/dashscope/developer-reference/api-details

## 📄 许可说明

本 AI 助手集成方案基于阿里云通义千问服务，使用时需遵守阿里云服务协议。
