import { ref, onUnmounted } from 'vue'

export function useChatWs(uid) {
  const ws = ref(null)
  const msgList = ref([])
  const connected = ref(false)

  function connect() {
    if (!uid) return
    // websocket地址，vite代理会转发到后端8080
    const url = `${location.origin.replace('http','ws')}/ws?uid=${uid}`
    ws.value = new WebSocket(url)

    ws.value.onopen = () => {
      connected.value = true
      console.log('✅ ws连接成功 uid=', uid)
    }

    ws.value.onmessage = (event) => {
      console.log('📩收到后端消息：', event.data)
      const data = JSON.parse(event.data)
      msgList.value.push(data)
    }

    ws.value.onclose = (e) => {
      connected.value = false
      console.log('❌ ws断开', e)
    }

    ws.value.onerror = (err) => {
      console.error('❌ ws错误', err)
    }
  }

  function sendWsMsg(payload) {
    if (!ws.value || ws.value.readyState !== WebSocket.OPEN) {
      console.warn('⚠️ websocket未连接，不能发送')
      return
    }
    console.log('📤发送消息：', payload)
    ws.value.send(JSON.stringify(payload))
  }

  // 发送文本消息 对齐后端字段
  function sendText(receiverUid, text) {
    sendWsMsg({
      senderUid: uid,
      receiverUid: receiverUid,
      content: text,
      msgType: 1
    })
  }

  // 发送图片
  async function sendImage(receiverUid, file) {
    const form = new FormData()
    form.append('file', file)
    const resp = await fetch('/upload/image', {
      method: 'POST',
      body: form
    })
    const json = await resp.json()
    const imgUrl = json.data
    sendWsMsg({
      senderUid: uid,
      receiverUid: receiverUid,
      content: imgUrl,
      msgType: 2
    })
  }

  function close() {
    if (ws.value) {
      ws.value.close()
    }
  }

  onUnmounted(() => {
    close()
  })

  return {
    connect,
    close,
    sendText,
    sendImage,
    msgList,
    connected
  }
}