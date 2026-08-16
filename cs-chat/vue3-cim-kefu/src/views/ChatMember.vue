<template>
  <div style="width:720px;border:1px solid #aaa;padding:16px;margin-top:10px">
    <h3>会员端 uid=member_001</h3>
    <button @click="connectWs">连接WS</button>
    <span style="margin-left:10px;color:green" v-if="connected">✅已连接</span>
    <span style="margin-left:10px;color:red" v-else>❌未连接</span>

    <div ref="scrollWrap" style="height:380px;border:1px solid #ccc;margin:12px 0;padding:8px;overflow-y:auto">
      <div v-for="(msg,i) in msgList" :key="i" style="margin:8px 0">
        <b>{{msg.senderUid}}：</b> {{msg.content}}
        <div style="font-size:12px;color:#888">{{new Date(msg.timestamp).toLocaleString()}}</div>
      </div>
    </div>

    <input v-model="inputText" placeholder="输入消息" @keyup.enter="sendMsg"/>
    <button @click="sendMsg">发送给客服kefu_001</button>
  </div>
</template>

<script setup>
import {ref,onUnmounted,nextTick} from 'vue'
let ws = null
const myUid = 'member_001'
const targetUid = 'kefu_001'

const connected = ref(false)
const msgList = ref([])
const inputText = ref('')
const scrollWrap = ref(null)

let pingTimer = null
let reconnectTimer = null
const PING_INTERVAL = 25000
const MAX_RETRY = 5
let retryCount = 0

function scrollBottom(){
  nextTick(()=>{
    if(scrollWrap.value){
      scrollWrap.value.scrollTop = scrollWrap.value.scrollHeight
    }
  })
}

function startPing(){
  stopPing()
  pingTimer = setInterval(()=>{
    if(ws && ws.readyState === WebSocket.OPEN){
      ws.send(JSON.stringify({cmd:99}))
    }
  },PING_INTERVAL)
}

function stopPing(){
  if(pingTimer){
    clearInterval(pingTimer)
    pingTimer = null
  }
}

function connectWs(){
  stopPing()
  clearTimeout(reconnectTimer)
  if(ws) ws.close()
  ws = new WebSocket(`ws://127.0.0.1:8080/ws?uid=${myUid}`)

  ws.onopen = ()=>{
    connected.value = true
    retryCount = 0
    startPing()
    console.log("会员ws打开成功")
  }

  ws.onmessage = (ev)=>{
    const obj = JSON.parse(ev.data)
    if(obj.cmd === 99 || obj.cmd ===100) return
    msgList.value.push(obj)
    scrollBottom()
  }

  ws.onclose = ()=>{
    connected.value = false
    stopPing()
    console.log("会员ws关闭，准备重连")
    if(retryCount < MAX_RETRY){
      retryCount++
      reconnectTimer = setTimeout(()=>connectWs(),3000)
    }
  }

  ws.onerror = e=>{
    console.error("会员ws错误",e)
  }
}

function sendMsg(){
  if(!ws || ws.readyState !== WebSocket.OPEN){
    alert("ws未连接！")
    return
  }
  const payload = {
    senderUid: myUid,
    receiverUid: targetUid,
    content: inputText.value,
    msgType:1
  }
  msgList.value.push({...payload,timestamp:Date.now()})
  scrollBottom()
  ws.send(JSON.stringify(payload))
  inputText.value = ''
}

onUnmounted(()=>{
  stopPing()
  clearTimeout(reconnectTimer)
  if(ws) ws.close()
})
</script>