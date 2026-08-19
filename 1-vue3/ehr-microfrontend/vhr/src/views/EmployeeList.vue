<template>
  <div class="employee-list">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-size: 16px; font-weight: bold;">员工列表</span>
          <el-button type="primary" @click="handleAdd">新增员工</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px;">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.dept" placeholder="请选择部门" clearable>
            <el-option label="技术部" value="tech" />
            <el-option label="产品部" value="product" />
            <el-option label="HR 部" value="hr" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="empNo" label="工号" width="120" />
        <el-table-column prop="dept" label="部门" width="120" />
        <el-table-column prop="position" label="岗位" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === '在职' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入职日期" width="140" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.id)">查看</el-button>
            <el-button type="warning" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">离职</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; text-align: right;"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next, jumper"
        @current-change="fetchList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const searchForm = reactive({ name: '', dept: '' });
const pagination = reactive({ page: 1, size: 10, total: 0 });
const tableData = ref([]);

const fetchList = async () => {
  loading.value = true;
  setTimeout(() => {
    tableData.value = [
      { id: 1, name: '张三', empNo: 'EMP001', dept: '技术部', position: '前端工程师', status: '在职', entryDate: '2023-01-15' },
      { id: 2, name: '李四', empNo: 'EMP002', dept: '产品部', position: '产品经理', status: '在职', entryDate: '2022-06-01' },
      { id: 3, name: '王五', empNo: 'EMP003', dept: 'HR 部', position: 'HRBP', status: '离职', entryDate: '2021-03-10' },
    ];
    pagination.total = 3;
    loading.value = false;
  }, 500);
};

const goDetail = (id) => router.push(`/employee/detail/${id}`);
const handleAdd = () => ElMessage.info('打开新增员工弹窗');
const handleEdit = (row) => ElMessage.info(`编辑员工：${row.name}`);
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认将 ${row.name} 标记为离职？`, '提示', { type: 'warning' })
    .then(() => { ElMessage.success('操作成功'); fetchList(); });
};

onMounted(fetchList);
</script>

<style lang="less" scoped>
.employee-list { padding: 16px; }
</style>
