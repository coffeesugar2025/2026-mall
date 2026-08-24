<template>
  <div class="invest">
    <el-card shadow="never">
      <template #header><b>投资决策（Plan-and-Execute Agent · IRR/NPV 工具）</b></template>
      <el-form :inline="true">
        <el-form-item label="项目"><el-input v-model="project" placeholder="如 新产品线" /></el-form-item>
        <el-form-item label="初始投入(万)"><el-input-number v-model="inv" :min="1" :max="9999" /></el-form-item>
      </el-form>
      <el-input v-model="text" type="textarea" :rows="4" placeholder="如：评估投入 500 万做新产品线、预计 3 年回本的可行性" />
      <div class="row">
        <el-button type="primary" :loading="loading" @click="advise">生成投资建议</el-button>
      </div>
      <div v-if="advice" class="result">
        <el-descriptions :column="1" border class="mt">
          <el-descriptions-item label="结论"><el-tag :type="advice.feasible ? 'success' : 'danger'">{{ advice.feasible ? '建议推进' : '建议谨慎' }}</el-tag></el-descriptions-item>
          <el-descriptions-item label="IRR">{{ advice.irr != null ? (advice.irr*100).toFixed(2)+'%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="NPV">{{ advice.npv != null ? advice.npv.toFixed(0)+' 万' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="投资理由">{{ advice.reason }}</el-descriptions-item>
          <el-descriptions-item label="主要风险"><el-tag v-for="r in advice.risks" :key="r" type="warning" class="tag">{{ r }}</el-tag></el-descriptions-item>
        </el-descriptions>
        <div ref="chart" class="chart"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import * as echarts from 'echarts'
import { api } from '../api'
const props = defineProps({ userId: String })
const project = ref('新产品线')
const inv = ref(500)
const text = ref('评估投入 500 万做新产品线、预计 3 年回本的可行性，给出 IRR 和建议')
const loading = ref(false)
const advice = ref(null)
const chart = ref(null)
let inst = null
async function advise() {
  loading.value = true
  try { advice.value = await api.advise(props.userId, project.value, text.value); renderChart() }
  finally { loading.value = false }
}
function renderChart() {
  if (!chart.value) return
  if (!inst) inst = echarts.init(chart.value)
  const irr = advice.value?.irr || 0
  inst.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['初始投入', '第1年', '第2年', '第3年'] },
    yAxis: { type: 'value', name: '万' },
    series: [{ type: 'bar', barWidth: '45%',
      data: [-inv.value, Math.round(inv.value*irr), Math.round(inv.value*irr*1.2), Math.round(inv.value*irr*1.4)],
      itemStyle: { color: '#1677ff', borderRadius: [6,6,0,0] },
      label: { show: true, position: 'top', formatter: '{c}' } }]
  })
}
watch(advice, renderChart)
</script>

<style scoped>
.row { margin: 12px 0; }
.mt { margin-top: 18px; }
.tag { margin-right: 6px; }
.chart { width: 100%; height: 320px; margin-top: 18px; }
</style>
