import axios from 'axios'
const http = axios.create({ baseURL: '/api', timeout: 120000 })

// 流式对话（SSE），返回 EventSource 风格的 fetch 流
export async function streamChat(userId, message, onToken, onDone, onError) {
  try {
    const resp = await fetch(`/api/ai/chat?userId=${encodeURIComponent(userId)}`, {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain' },
      body: message
    })
    if (!resp.ok) throw new Error('HTTP ' + resp.status)
    const reader = resp.body.getReader()
    const decoder = new TextDecoder()
    let buf = ''
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buf += decoder.decode(value, { stream: true })
      // SSE: 按事件帧拆分
      const frames = buf.split('\n\n')
      buf = frames.pop()
      for (const f of frames) {
        const line = f.trim()
        if (!line) continue
        if (line.startsWith('data:')) onToken(line.slice(5).trim())
      }
    }
    onDone && onDone()
  } catch (e) { onError && onError(e) }
}

export const api = {
  analyze: (userId, text) => http.post(`/ai/analyze?userId=${encodeURIComponent(userId)}`, text, { headers: { 'Content-Type': 'text/plain' } }).then(r => r.data),
  advise: (userId, project, text) => http.post(`/ai/invest/advise?userId=${encodeURIComponent(userId)}&project=${encodeURIComponent(project)}`, text, { headers: { 'Content-Type': 'text/plain' } }).then(r => r.data),
  ingest: (dir) => http.post(`/ai/rag/ingest?dir=${encodeURIComponent(dir)}`),
  ingestText: (text) => http.post(`/ai/rag/ingest-text`, text, { headers: { 'Content-Type': 'text/plain' } }),
  search: (q) => http.get(`/ai/rag/search`, { params: { q } }).then(r => r.data),
  health: () => http.get('/actuator/health').then(r => r.data).catch(() => null)
}
export default http
