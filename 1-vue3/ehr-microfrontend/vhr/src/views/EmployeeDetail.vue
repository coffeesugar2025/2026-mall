<template>
  <el-card v-loading="loading">
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 16px; font-weight: bold;">员工档案</span>
        <el-button @click="router.back()">返回</el-button>
      </div>
    </template>

    <el-descriptions v-if="employee.id" :column="2" border>
      <el-descriptions-item label="姓名">{{ employee.name }}</el-descriptions-item>
      <el-descriptions-item label="工号">{{ employee.empNo }}</el-descriptions-item>
      <el-descriptions-item label="部门">{{ employee.dept }}</el-descriptions-item>
      <el-descriptions-item label="岗位">{{ employee.position }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="employee.status === '在职' ? 'success' : 'danger'">
          {{ employee.status }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="入职日期">{{ employee.entryDate }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="暂无数据" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const employee = ref({});

onMounted(() => {
  loading.value = true;
  const id = route.params.id;
  setTimeout(() => {
    const mock = {
      1: { id: 1, name: '张三', empNo: 'EMP001', dept: '技术部', position: '前端工程师', status: '在职', entryDate: '2023-01-15' },
      2: { id: 2, name: '李四', empNo: 'EMP002', dept: '产品部', position: '产品经理', status: '在职', entryDate: '2022-06-01' },
      3: { id: 3, name: '王五', empNo: 'EMP003', dept: 'HR 部', position: 'HRBP', status: '离职', entryDate: '2021-03-10' },
    };
    employee.value = mock[id] || {};
    loading.value = false;
  }, 500);
});
</script>

<style scoped>
.el-descriptions { margin: 16px; }
</style>
