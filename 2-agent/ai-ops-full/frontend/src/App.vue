<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="26" color="#409EFF"><Cpu /></el-icon>
        <span>AI Ops Hub</span>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/chat"><el-icon><ChatDotRound /></el-icon><span>智能对话</span></el-menu-item>
        <el-menu-item index="/knowledge"><el-icon><Files /></el-icon><span>知识库</span></el-menu-item>
        <el-menu-item index="/incident"><el-icon><WarningFilled /></el-icon><span>事件分析</span></el-menu-item>
        <el-menu-item index="/invest"><el-icon><TrendCharts /></el-icon><span>投资决策</span></el-menu-item>
        <el-menu-item index="/monitor"><el-icon><DataLine /></el-icon><span>系统监控</span></el-menu-item>
      </el-menu>
      <div class="aside-foot">
        <el-tag type="success" effect="dark" round>LangChain4j 1.19</el-tag>
      </div>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-title">{{ pageTitle }}</div>
        <div class="header-user">
          <el-tag>用户：{{ userId }}</el-tag>
          <el-button text @click="userId = prompt('切换用户ID', userId) || userId">切换</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <component :is="Component" :userId="userId" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const userId = ref('ops-001')
const titles = {
  '/chat': '智能对话（ReAct / Supervisor 多 Agent）',
  '/knowledge': '企业知识库（RAG 摄取 + 检索 + ReRank）',
  '/incident': '事件分析（结构化输出 + 自动建工单）',
  '/invest': '投资决策（Plan-and-Execute Agent + IRR）',
  '/monitor': '系统监控（Token 用量 / 调用审计）'
}
const pageTitle = computed(() => titles[route.path] || '企业智能运营中枢')
</script>

<style>
html, body, #app { height: 100%; margin: 0; }
.layout { height: 100%; }
.aside { background: #001529; color: #fff; display: flex; flex-direction: column; }
.logo { display: flex; align-items: center; gap: 10px; padding: 18px; font-size: 18px; font-weight: 700; color: #fff; border-bottom: 1px solid #1f2d3d; }
.menu { flex: 1; border-right: 0; background: transparent; }
.el-menu-item { color: #c9d3df !important; }
.el-menu-item.is-active { background: #1677ff !important; color: #fff !important; }
.aside-foot { padding: 14px; text-align: center; }
.header { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e8eaed; background: #fff; }
.header-title { font-size: 16px; font-weight: 600; }
.header-user { display: flex; align-items: center; gap: 8px; }
.main { background: #f4f6f9; padding: 18px; }
</style>
