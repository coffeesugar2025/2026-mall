<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-bottom: 24px;">EHR 系统登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="admin123" />
        </el-form-item>
        <el-button type="primary" style="width: 100%;" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const store = useStore();
const router = useRouter();
const form = reactive({ username: '', password: '' });

const handleLogin = () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码');
    return;
  }
  store.commit('SET_TOKEN', 'mock-jwt-token');
  store.commit('SET_USER', { name: form.username, role: 'HR' });
  ElMessage.success('登录成功');
  router.push('/dashboard');
};
</script>

<style scoped>
.login-wrap {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card { width: 400px; padding: 20px; }
</style>
