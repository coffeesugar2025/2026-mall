<template>
  <div class="kb">
    <el-card shadow="never">
      <template #header><b>RAG 知识库管理</b></template>
      <el-tabs v-model="tab">
        <el-tab-pane label="文档摄取" name="ingest">
          <el-input v-model="dir" placeholder="文档目录路径（后端可访问），如 /data/docs" />
          <div class="row">
            <el-button type="primary" @click="doIngest">摄取目录文档</el-button>
            <el-divider direction="vertical" />
            <span>或粘贴文本片段：</span>
            <el-input v-model="snippet" style="width:360px" placeholder="如：order-service 数据库连接池建议配置为 50" />
            <el-button @click="doIngestText">写入知识库</el-button>
          </div>
          <el-alert v-if="ingestMsg" :title="ingestMsg" type="success" show-icon class="mt" :closable="false" />
        </el-tab-pane>
        <el-tab-pane label="语义检索" name="search">
          <el-input v-model="q" placeholder="输入检索关键词，如 数据库连接池 配置" @keyup.enter="doSearch">
            <template #append><el-button @click="doSearch">检索（向量+ReRank）</el-button></template>
          </el-input>
          <div v-if="results.length" class="res">
            <el-card v-for="(r,i) in results" :key="i" class="res-card" shadow="hover">
              <div class="res-meta"><el-tag>TOP {{ i+1 }}</el-tag><span>相似度：{{ r.score }}</span></div>
              <div class="res-text">{{ r.text }}</div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { api } from '../api'
import { ElMessage } from 'element-plus'
const tab = ref('ingest')
const dir = ref('/data/docs')
const snippet = ref('')
const ingestMsg = ref('')
const q = ref('')
const results = ref([])
async function doIngest() {
  await api.ingest(dir.value); ingestMsg.value = '已提交目录摄取任务（后端异步处理）'; ElMessage.success('已提交')
}
async function doIngestText() {
  if (!snippet.value) return
  await api.ingestText(snippet.value); ElMessage.success('已写入知识库'); snippet.value = ''; ingestMsg.value = '文本已写入向量库'
}
async function doSearch() {
  if (!q.value) return
  const data = await api.search(q.value)
  results.value = (data.matches || []).map(m => ({ text: m.text, score: m.score?.toFixed?.(4) || '-' }))
}
</script>

<style scoped>
.row { display: flex; align-items: center; gap: 10px; margin-top: 12px; flex-wrap: wrap; }
.mt { margin-top: 14px; }
.res { margin-top: 16px; }
.res-card { margin-bottom: 10px; }
.res-meta { display: flex; gap: 10px; align-items: center; margin-bottom: 6px; color: #666; font-size: 12px; }
.res-text { white-space: pre-wrap; line-height: 1.6; }
</style>
