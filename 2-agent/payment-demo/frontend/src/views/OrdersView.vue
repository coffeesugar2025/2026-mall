<template>
  <div class="orders-view">
    <div class="page-header">
      <h2 class="page-title">📋 订单管理</h2>
      <div class="filter-bar">
        <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="loadOrders">
          <el-option label="待支付" value="PENDING" />
          <el-option label="已支付" value="PAID" />
          <el-option label="支付失败" value="FAILED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-button @click="loadOrders" :icon="Refresh">刷新</el-button>
      </div>
    </div>

    <el-table :data="orders" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="productName" label="商品名称" min-width="200" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">
          <span class="amount">¥{{ Number(row.amount).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.payType === 'ALIPAY'" type="primary" size="small">支付宝</el-tag>
          <el-tag v-else type="success" size="small">微信</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'PENDING'"
            type="primary"
            size="small"
            @click="goPay(row)"
          >
            去支付
          </el-button>
          <el-button
            v-if="row.status === 'PENDING'"
            type="danger"
            size="small"
            @click="handleClose(row)"
          >
            关闭
          </el-button>
          <el-button
            v-if="row.status !== 'PENDING'"
            size="small"
            @click="viewDetail(row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getOrders, closeOrder } from '@/api/payment'

const router = useRouter()

const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders(currentPage.value, pageSize.value, statusFilter.value)
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载订单失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const goPay = (row) => {
  router.push({
    name: 'Checkout',
    params: { orderNo: row.orderNo },
    query: { payType: row.payType }
  })
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm(`确定关闭订单 ${row.orderNo} 吗？`, '确认关闭', {
      type: 'warning'
    })
    await closeOrder(row.orderNo)
    ElMessage.success('订单已关闭')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '关闭失败')
    }
  }
}

const viewDetail = (row) => {
  router.push({
    name: 'PayResult',
    query: { orderNo: row.orderNo, payType: row.payType }
  })
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', PAID: 'success', FAILED: 'danger', CLOSED: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { PENDING: '待支付', PAID: '已支付', FAILED: '失败', CLOSED: '已关闭' }
  return map[status] || status
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  color: #303133;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
