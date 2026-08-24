<template>
  <div class="chat">
    <div class="hint">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          Supervisor 多 Agent 已就绪：尝试"order-service 频繁 500 帮我分析原因并建工单"（运维 Agent）、"评估投入 500 万做新产品线"（投资 Agent）、"检查这段内容是否合规"（安全 Agent）。
        </template>
      </el-alert>
    </div>
    <div class="messages" ref="box">
      <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
        <el-icon class="avatar" :size="22"><User v-if="m.role==='user'"/><Cpu v-else/></el-icon>
        <div class="bubble">
          <div class="role">{{ m.role === 'user' ? '我' : 'AI Agent' }}</div>
          <div class="content" v-html="render(m.content)"></div>
          <div v-if="m.tool" class="tool">⚙️ 工具调用：{{ m.tool }}</div>
        </div>
      </div>
      <div v-if="loading" class="msg-row assistant"><el-icon :size="22"><Cpu/></el-icon><div class="bubble"><div class="content"><el-icon class="is-loading"><Loading/></el-icon> 思考中…</div></div></div>
    </div>
    <div class="input">
      <el-input v-model="input" type="textarea" :rows="3" placeholder="输入运维/投资/安全问题，Enter 发送（Shift+Enter 换行）" @keydown.enter.exact.prevent="send" />
      <el-button type="primary" :loading="loading" @click="send">发送</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { streamChat } from '../api'
import { marked } from 'marked'
const props = defineProps({ userId: String })
const messages = ref([{ role: 'assistant', content: '你好，我是企业智能运营中枢 Agent。可以帮你分析故障、检索知识库、评估投资、审核合规内容。' }])
const input = ref('')
const loading = ref(false)
const box = ref(null)
function render(t) { return marked.parse(t || '') }
async function send() {
  const text = input.value.trim(); if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''; loading.value = true
  await nextTick(); scroll()
  let buf = ''
  messages.value.push({ role: 'assistant', content: '' })
  await streamChat(props.userId, text,
    (tok) => { buf += tok; messages.value[messages.value.length-1].content = buf; scroll() },
    () => { loading.value = false },
    () => { loading.value = false; messages.value[messages.value.length-1].content += '\n\n_（请求异常，请检查后端服务）_' }
  )
}
function scroll() { nextTick(() => { if (box.value) box.value.scrollTop = box.value.scrollHeight }) }
</script>

<style scoped>
.chat { display: flex; flex-direction: column; height: calc(100vh - 130px); }
.hint { margin-bottom: 12px; }
.messages { flex: 1; overflow: auto; padding: 8px; }
.msg-row { display: flex; gap: 10px; margin-bottom: 14px; }
.msg-row.user { flex-direction: row-reverse; }
.avatar { margin-top: 4px; color: #409EFF; }
.bubble { max-width: 78%; background: #fff; border-radius: 8px; padding: 10px 14px; box-shadow: 0 1px 4px rgba(0,0,0,.06); }
.msg-row.user .bubble { background: #e8f3ff; }
.role { font-size: 12px; color: #999; margin-bottom: 4px; }
.content { line-height: 1.7; word-break: break-word; }
.content :deep(pre) { background: #0f172a; color: #e2e8f0; padding: 10px; border-radius: 6px; overflow: auto; }
.content :deep(code) { background: #f1f5f9; padding: 1px 4px; border-radius: 3px; }
.tool { margin-top: 6px; font-size: 12px; color: #1677ff; }
.input { display: flex; gap: 10px; border-top: 1px solid #e8eaed; padding-top: 12px; }
</style>
