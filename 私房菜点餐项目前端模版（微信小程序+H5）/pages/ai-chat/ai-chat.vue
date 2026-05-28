<template>
  <view class="ai-chat-page">
    <view class="chat-header">
      <view class="header-content">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="avatar-emoji">🤖</view>
        <view class="header-info">
          <text class="title">AI 点餐助手</text>
          <text class="subtitle">为您推荐适合的菜品与套餐</text>
        </view>
      </view>
    </view>

    <scroll-view
      class="chat-content"
      scroll-y
      :scroll-top="scrollTop"
      scroll-with-animation
      :scroll-into-view="scrollIntoView"
    >
      <view class="message-item ai-message" v-if="messages.length === 0">
        <view class="message-avatar">
          <view class="avatar-emoji-small">🤖</view>
        </view>
        <view class="message-content">
          <text>您好，我是七云菜馆的 AI 点餐助手。

我可以帮您：
1. 推荐招牌菜和套餐
2. 解答营业时间与配送范围
3. 根据口味偏好推荐菜品

可以直接告诉我您想吃辣、清淡，或者想找适合几个人的菜。</text>
        </view>
      </view>

      <view
        v-for="(msg, index) in messages"
        :key="index"
        :id="'msg-' + index"
        class="message-item"
        :class="msg.role === 'user' ? 'user-message' : 'ai-message'"
      >
        <view class="message-avatar" v-if="msg.role === 'assistant'">
          <view class="avatar-emoji-small">🤖</view>
        </view>
        <view class="message-content">
          <text>{{ msg.content }}</text>
        </view>
        <view class="message-avatar" v-if="msg.role === 'user'">
          <view class="avatar-emoji-small">👤</view>
        </view>
      </view>

      <view class="message-item ai-message" v-if="loading">
        <view class="message-avatar">
          <view class="avatar-emoji-small">🤖</view>
        </view>
        <view class="message-content loading">
          <view class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="quick-questions" v-if="messages.length === 0">
      <view
        v-for="(q, index) in quickQuestions"
        :key="index"
        class="quick-item"
        @click="sendQuickQuestion(q)"
      >
        {{ q }}
      </view>
    </view>

    <view class="chat-input">
      <input
        class="input-box"
        v-model="inputText"
        placeholder="输入您想咨询的问题"
        :disabled="loading"
        @confirm="sendMessage"
        confirm-type="send"
      />
      <view
        class="send-btn"
        :class="{ active: inputText.trim() && !loading }"
        @click="sendMessage"
      >
        发送
      </view>
    </view>
  </view>
</template>

<script>
import { chatWithAI } from '@/api/ai.js'

export default {
  data() {
    return {
      messages: [],
      inputText: '',
      loading: false,
      scrollTop: 0,
      scrollIntoView: '',
      quickQuestions: [
        '推荐几道招牌菜',
        '有什么适合两个人吃的套餐？',
        '营业到几点？',
        '可以配送吗？'
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack({ delta: 1 })
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.loading) return

      this.messages.push({
        role: 'user',
        content: text
      })
      this.inputText = ''
      this.scrollToBottom()

      this.loading = true
      try {
        const res = await chatWithAI(text, this.messages.slice(0, -1))
        if (res.success) {
          this.messages.push({
            role: 'assistant',
            content: res.message
          })
        } else {
          this.messages.push({
            role: 'assistant',
            content: '抱歉，我暂时没有理解这个问题，您可以换一种说法再试试。'
          })
        }
      } catch (error) {
        console.error('AI 调用失败:', error)
        this.messages.push({
          role: 'assistant',
          content: '当前网络或服务暂时不可用，请稍后再试。'
        })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },
    sendQuickQuestion(question) {
      this.inputText = question
      this.sendMessage()
    },
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 999999
        if (this.messages.length > 0) {
          this.scrollIntoView = 'msg-' + (this.messages.length - 1)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ai-chat-page {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fb;
}

.chat-header {
  background: linear-gradient(135deg, #ee2f37, #f97316);
  padding: calc(var(--status-bar-height) + 20rpx) 24rpx 24rpx;
  color: #fff;
}

.header-content {
  display: flex;
  align-items: center;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.back-icon {
  font-size: 48rpx;
  font-weight: 300;
}

.avatar-emoji {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50rpx;
  background: rgba(255, 255, 255, 0.2);
}

.header-info {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
}

.subtitle {
  font-size: 24rpx;
  opacity: 0.9;
  margin-top: 8rpx;
}

.chat-content {
  flex: 1;
  padding: 24rpx;
}

.message-item {
  display: flex;
  margin-bottom: 32rpx;
  animation: messageIn 0.3s ease-out;
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.avatar-emoji-small {
  font-size: 40rpx;
}

.message-content {
  max-width: 500rpx;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

.message-content.loading {
  background: #fff;
  padding: 24rpx;
}

@keyframes messageIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message .message-avatar {
  margin-right: 20rpx;
}

.ai-message .message-content {
  background: #fff;
  color: #333;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-avatar {
  margin-left: 20rpx;
}

.user-message .message-content {
  background: linear-gradient(135deg, #ee2f37, #f97316);
  color: #fff;
}

.loading-dots {
  display: flex;
  gap: 12rpx;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  background: #ee2f37;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.quick-questions {
  padding: 20rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.quick-item {
  padding: 16rpx 28rpx;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border-radius: 24rpx;
  font-size: 26rpx;
  color: #9a3412;
  border: 1rpx solid #fed7aa;
}

.chat-input {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1rpx solid #eee;
}

.input-box {
  flex: 1;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f5f5;
  border-radius: 36rpx;
  font-size: 28rpx;
}

.send-btn {
  margin-left: 20rpx;
  padding: 16rpx 40rpx;
  background: #ddd;
  color: #999;
  border-radius: 36rpx;
  font-size: 28rpx;
}

.send-btn.active {
  background: linear-gradient(135deg, #ee2f37, #f97316);
  color: #fff;
}
</style>
