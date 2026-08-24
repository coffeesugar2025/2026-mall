<template>
  <div class="incident">
    <el-card shadow="never">
      <template #header><b>事件分析（结构化输出 POJO + 自动建工单）</b></template>
      <el-input v-model="text" type="textarea" :rows="5" placeholder="描述事件，如：order-service 连续 30 分钟 500 错误，疑似 DB 连接池耗尽" />
      <div class="row">
        <el-button type="primary" :loading="loading" @click="analyze">生成结构化报告</el-button>
      </div>
      <el-descriptions v-if="report" :column="1" border class="mt">
        <el-descriptions-item label="根因推断">{{ report.rootCause }}</el-descriptions-item>
        <el-descriptions-item label="影响范围">{{ report.impact }}</el-descriptions-item>
        <el-descriptions-item label="处置建议">{{ report.suggestion }}</el-descriptions-item>
        <el-descriptions-item label="建议动作"><el-tag v-for="a in report.actions" :key="a" class="tag">{{ a }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="工单号"><el-tag type="success">{{ report.ticketId }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="风险等级"><el-tag :type="riskType(report.riskLevel)">{{ report.riskLevel }}</el-tag></el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'
const props = defineProps({ userId: String })
const text = ref('order-service 连续 30 分钟 500 错误，疑似 DB 连接池耗尽')
const loading = ref(false)
const report = ref(null)
async function analyze() {
  loading.value = true
  try { report.value = await api.analyze(props.userId, text.value) }
  finally { loading.value = false }
}
function riskType(l) { return ({ HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' })[l] || '' }
</script>

<style scoped>
.row { margin-top: 12px; }
.mt { margin-top: 18px; }
.tag { margin-right: 6px; }
</style>
