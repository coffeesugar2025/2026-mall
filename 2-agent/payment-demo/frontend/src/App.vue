<template>
  <div id="app-container">
    <!-- 顶部导航 -->
    <el-header class="app-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <el-icon :size="24" color="#409eff"><ShoppingCart /></el-icon>
          <span class="logo-text">支付演示商城</span>
        </router-link>
        <el-menu mode="horizontal" :router="true" class="nav-menu" :default-active="$route.path">
          <el-menu-item index="/">商品列表</el-menu-item>
          <el-menu-item index="/orders">订单管理</el-menu-item>
        </el-menu>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="app-footer">
      <p>支付演示项目 · Vue3 + Spring Boot 3 · 支付宝沙箱 + 微信支付 Mock</p>
    </el-footer>
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f7fa;
}

#app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 40px;
  height: 60px;
  line-height: 60px;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  text-decoration: none;
  margin-right: 40px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-left: 8px;
}

.nav-menu {
  flex: 1;
  border-bottom: none !important;
}

.app-main {
  flex: 1;
  padding: 24px 40px;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
}

.app-footer {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
