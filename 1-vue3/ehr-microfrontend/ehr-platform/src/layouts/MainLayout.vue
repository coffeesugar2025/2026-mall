<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h3>EHR 管理平台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <!-- 子应用菜单：index 只写主应用路由，不要写 localhost:8081 -->
        <el-sub-menu index="/vhr">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>员工管理</span>
          </template>
          <el-menu-item index="/vhr/employee/list">员工列表</el-menu-item>
          <el-menu-item index="/vhr/employee/detail">员工档案</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span>当前用户：{{ user?.name || '未登录' }}</span>
        <el-dropdown>
          <el-avatar>{{ user?.name?.[0] || 'U' }}</el-avatar>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main>
        <!-- 主应用自己的页面 -->
        <router-view v-if="!isVhrRoute" />

        <!-- 子应用挂载容器：始终在 DOM 中，v-show 只是隐藏不销毁 -->
        <div
          v-show="isVhrRoute"
          id="subapp-viewport"
          class="subapp-container"
        />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { HomeFilled, UserFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const store = useStore();

const user = computed(() => store.state.user);
const isVhrRoute = computed(() => route.path.startsWith('/vhr'));
const activeMenu = computed(() => {
  if (route.path.startsWith('/vhr')) return '/vhr';
  return route.path;
});

const logout = () => {
  store.commit('CLEAR_USER');
  router.push('/login');
};
</script>

<style lang="less" scoped>
.layout { height: 100vh; }
.sidebar {
  background: #001529;
  .logo {
    padding: 16px;
    color: #fff;
    text-align: center;
    border-bottom: 1px solid #1f2d3d;
    h3 { margin: 0; font-size: 16px; }
  }
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}
.subapp-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}
</style>