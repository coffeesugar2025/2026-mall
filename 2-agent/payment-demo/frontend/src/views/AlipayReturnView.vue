<template>
  <div class="alipay-return-view">
    <el-result
      icon="info"
      title="正在确认支付结果..."
      sub-title="请稍候，正在与服务器确认您的支付状态"
    >
      <template #extra>
        <el-button type="primary" @click="checkStatus">立即查询</el-button>
        <el-button @click="$router.push('/orders')">查看订单列表</el-button>
      </template>
    </el-result>

    <div v-if="order" class="return-detail">
      <el-card shadow="hover">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(order.status)">{{ statusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ Number(order.amount).toFixed(2) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { queryOrder } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const order = ref(null)

const orderNo = route.query.orderNo

onMounted(() => {
  debugger;
  if (orderNo) {
    checkStatus()
  } else {
    ElMessage.warning('缺少订单号')
    router.push('/')
  }
})

const checkStatus = async () => {
  if (!orderNo) return

  try {
    const res = await queryOrder(orderNo)
    order.value = res.data
    debugger;
    if (res.data.status === 'PAID') {
      ElMessage.success('🎉 支付成功！')
      setTimeout(() => {
        router.push({ name: 'PayResult', query: { orderNo, payType: 'ALIPAY' } })
      }, 1500)
    } else if (res.data.status === 'PENDING') {
      ElMessage.info('订单尚未支付完成，请稍候再试')
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  }
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', PAID: 'success', FAILED: 'danger', CLOSED: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { PENDING: '待支付', PAID: '已支付', FAILED: '失败', CLOSED: '已关闭' }
  return map[status] || status
}
</script>

<style scoped>
.alipay-return-view {
  max-width: 600px;
  margin: 40px auto;
}

.return-detail {
  margin-top: 24px;
}
</style>
