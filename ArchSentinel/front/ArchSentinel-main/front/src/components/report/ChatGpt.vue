<template>
    <div class="chat-container">
      <h2>OpenAI GPT 聊天示例（Vue 2）</h2>
      <textarea v-model="userInput" placeholder="请输入你的问题" rows="5"></textarea>
      <button @click="sendMessage">发送</button>
      <div v-if="loading">加载中...</div>
      <div v-if="response" class="response">
        <strong>AI 回复：</strong>
        <p>{{ response }}</p>
      </div>
    </div>
</template>

<script>
import api_key from "../../../public/api_key.json"


export default {
  data() {
      return {
        userInput: '',
        response: '',
        loading: false,
        // ⚠️ 请替换为你自己的 OpenAI API Key，仅限学习用途
        API_KEY: api_key.API_KEY
      }
  },
  methods: {
    async sendMessage() {
        if (!this.userInput.trim()) return
        this.loading = true
        this.response = ''
  
        try {
          const res = await fetch('https://xiaoai.plus/v1/chat/completions', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              Authorization: `Bearer ${this.API_KEY}`
            },
            body: JSON.stringify({
              model: 'gpt-3.5-turbo',
              messages: [{ role: 'user', content: this.userInput }]
            })
          })
  
          const data = await res.json()
          this.response =
            data.choices?.[0]?.message?.content?.trim() || '出错了。'
        } catch (err) {
          this.response = '请求失败，请检查网络或 API 设置。'
        } finally {
          this.loading = false
        }
      }
    }
  }
</script>

<style scoped>
.chat-container {
    max-width: 600px;
    margin: 2rem auto;
    font-family: Arial, sans-serif;
}
textarea {
    width: 100%;
    margin-bottom: 1rem;
}
.response {
    background: #f4f4f4;
    padding: 1rem;
    border-radius: 6px;
    margin-top: 1rem;
}
</style>
  