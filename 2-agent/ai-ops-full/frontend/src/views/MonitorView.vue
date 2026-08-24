<template>
  <div class="monitor">
    <el-row :gutter="16">
      <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ stats.calls }}</div><div class="lbl">Agent 调用次数</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ stats.tokens }}</div><div class="lbl">累计 Token</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ stats.errors }}</div><div class="lbl">异常次数</div></div></el-card></el-col>
      <el-col :span="6"><el-card shadow="hover"><div class="kpi"><div class="num">{{ stats.avgMs }}ms</div><div class="lbl">平均耗时</div></div></el-card></el-col>
    </el-row>
    <el-card shadow="never" class="mt">
      <template #header><b>近 7 日调用量趋势</b></template>
      <div ref="trend" class="chart"></div>
    </el-card>
    <el-card shadow="never" class="mt">
      <template #header><b>调用审计日志</b></template>
      <el-table :data="logs" stripe max-height="360">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="user" label="用户" width="120" />
        <el-table-column prop="agent" label="Agent" width="140" />
        <el-table-column prop="tool" label="工具" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}"><el-tag :type="row.status==='OK'?'success':'danger'">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="ms" label="耗时(ms)" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
const stats = ref({ calls: 128, tokens: 86420, errors: 3, avgMs: 1240 })
const logs = ref([
  { time: '2026-08-24 10:12', user: 'ops-001', agent: 'Supervisor', tool: 'queryLogs', status: 'OK', ms: 980 },
  { time: '2026-08-24 10:15', user: 'ops-001', agent: 'OpsAgent', tool: 'createTicket', status: 'OK', ms: 620 },
  { time: '2026-08-24 10:21', user: 'biz-01', agent: 'InvestAgent', tool: 'calcIRR', status: 'OK', ms: 1540 },
  { time: '2026-08-24 10:33', user: 'sec-01', agent: 'SecurityAgent', tool: 'auditLog', status: 'FAIL', ms: 2100 }
])
const trend = ref(null)
onMounted(() => {
  if (trend.value) {
    const c = echarts.init(trend.value)
    c.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['D1','D2','D3','D4','D5','D6','D7'] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, data: [12,28,19,35,42,30,48], areaStyle: {}, itemStyle: { color: '#1677ff' } }]
    })
    window.addEventListener('resize', () => c.resize())
  }
})
</script>

<style scoped>
.kpi { text-align: center; padding: 8px 0; }
.num { font-size: 26px; font-weight: 700; color: #1677ff; }
.lbl { font-size: 13px; color: #888; margin-top: 4px; }
.mt { margin-top: 16px; }
.chart { width: 100%; height: 300px; }
</style>
