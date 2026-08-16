<template>
  <div style="padding:20px">
    <button @click="view='member'">会员端 member_001</button>
    <button @click="view='admin'" style="margin-left:10px">客服端 kefu_001</button>
    <ChatMember v-if="view === 'member'"/>
    <ChatAdmin v-if="view === 'admin'"/>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ChatMember from '@/views/ChatMember.vue'
import ChatAdmin from '@/views/ChatAdmin.vue'
const view = ref('member')
</script>