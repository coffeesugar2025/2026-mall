<template>
  <div class="pay-result-view" v-if="order">
    <!-- 支付成功 -->
    <el-result
      v-if="order.status === 'PAID'"
      icon="success"
      title="支付成功！"
      :sub-title="`订单号: ${order.orderNo} | 金额: ¥${order.amount.toFixed(2)}`"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">继续购物</el-button>
        <el-button @click="$router.push('/orders')">查看订单</el-button>
      </template>
    </el-result>

    <!-- 支付失败 -->
    <el-result
      v-else-if="order.status === 'FAILED'"
      icon="error"
      title="支付失败"
      sub-title="请重新尝试支付"
    >
      <template #extra>
        <el-button type="primary" @click="retryPay">重新支付</el-button>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <!-- 已关闭 -->
    <el-result
      v-else-if="order.status === 'CLOSED'"
      icon="warning"
      title="订单已关闭"
      sub-title="订单超时未支付，已自动关闭"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <!-- 待支付（轮询中） -->
    <el-result
      v-else
      icon="info"
      title="等待支付确认..."
      :sub-title="`订单号: ${order.orderNo}`"
    >
      <template #extra>
        <el-button type="primary" @click="refreshOrder">刷新状态</el-button>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <!-- 订单详情 -->
    <el-card class="result-detail" shadow="hover" v-if="order.status === 'PAID'">
      <template #header>
        <span>📄 订单详情</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ order.productName }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">¥{{ order.amount.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag v-if="order.payType === 'ALIPAY'" type="primary">支付宝</el-tag>
          <el-tag v-else type="success">微信支付</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易流水号">{{ order.tradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>

  <!-- 加载中 -->
  <div v-else class="loading-area">
    <el-skeleton :rows="4" animated />
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

onMounted(async () => {
  const orderNo = route.query.orderNo
  if (!orderNo) {
    ElMessage.warning('缺少订单号参数')
    router.push('/')
    return
  }

  await loadOrder(orderNo)
})

const loadOrder = async (orderNo) => {
  try {
    const res = await queryOrder(orderNo)
    order.value = res.data
  } catch (error) {
    ElMessage.error('加载订单失败: ' + error.message)
  }
}

const refreshOrder = () => {
  if (order.value) {
    loadOrder(order.value.orderNo)
  }
}

const retryPay = () => {
  if (order.value) {
    router.push({
      name: 'Checkout',
      params: { orderNo: order.value.orderNo },
      query: { payType: order.value.payType }
    })
  }
}
</script>

<style scoped>
.pay-result-view {
  max-width: 600px;
  margin: 40px auto;
}

.result-detail {
  margin-top: 24px;
}
</style>
