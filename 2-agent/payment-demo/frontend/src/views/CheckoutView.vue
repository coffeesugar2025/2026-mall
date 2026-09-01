<template>
  <div class="checkout-view" v-if="order">
    <!-- 订单信息卡片 -->
    <el-card class="order-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>📋 订单信息</span>
          <el-tag :type="statusTagType(order.status)">{{ statusText(order.status) }}</el-tag>
        </div>
      </template>

      <div class="order-info">
        <div class="info-row">
          <span class="label">订单号：</span>
          <span class="value">{{ order.orderNo }}</span>
          <el-button size="small" @click="copyText(order.orderNo)">复制</el-button>
        </div>
        <div class="info-row">
          <span class="label">商品名称：</span>
          <span class="value">{{ order.productName }}</span>
        </div>
        <div class="info-row">
          <span class="label">支付方式：</span>
          <span class="value">
            <el-tag v-if="order.payType === 'ALIPAY'" type="primary">支付宝</el-tag>
            <el-tag v-else type="success">微信支付</el-tag>
          </span>
        </div>
        <div class="info-row">
          <span class="label">订单金额：</span>
          <span class="value amount">¥{{ order.amount.toFixed(2) }}</span>
        </div>
        <div class="info-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ order.createTime }}</span>
        </div>
      </div>
    </el-card>

    <!-- 支付宝支付区域 -->
    <el-card v-if="order.payType === 'ALIPAY'" class="pay-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>💰 支付宝支付（沙箱环境）</span>
        </div>
      </template>

      <div class="alipay-area">
        <el-alert
          title="即将跳转到支付宝沙箱支付页面"
          description="请使用支付宝沙箱版 APP 或沙箱账号扫码/登录支付。沙箱环境仅用于开发测试。"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <div class="alipay-actions">
          <el-button type="primary" size="large" @click="launchAlipay">
            <el-icon><Money /></el-icon> 立即支付
          </el-button>
          <el-button @click="$router.push('/')">返回商品列表</el-button>
        </div>

        <!-- 支付宝表单将注入到这里 -->
        <div ref="alipayFormContainer" class="alipay-form-container"></div>
      </div>
    </el-card>

    <!-- 微信支付区域（Mock） -->
    <el-card v-if="order.payType === 'WECHAT'" class="pay-card" shadow="always">
      <template #header>
        <div class="card-header">
          <span>💚 微信支付（Mock 模式）</span>
        </div>
      </template>

      <div class="wechat-area">
        <el-alert
          title="当前为 Mock 模拟支付"
          description="非真实微信支付，仅用于演示完整支付流程。系统将在几秒后自动模拟支付成功。"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <div class="qrcode-area">
          <div class="qrcode-placeholder">
            <el-icon :size="48" color="#67c23a"><ChatDotRound /></el-icon>
            <p>模拟微信支付二维码</p>
            <p class="qrcode-tip">二维码内容：{{ qrContent || '生成中...' }}</p>
          </div>
        </div>

        <div class="wechat-actions">
          <el-button type="success" size="large" @click="mockPayNow">
            <el-icon><Check /></el-icon> 模拟立即支付成功
          </el-button>
          <el-button @click="$router.push('/')">返回商品列表</el-button>
        </div>

        <el-divider>或者等待自动模拟（3~8秒）</el-divider>

        <div class="auto-pay-status">
          <el-progress
            v-if="autoPaying"
            :percentage="autoPayProgress"
            :indeterminate="true"
            status="warning"
            :stroke-width="6"
          />
          <p v-if="autoPaying" class="auto-pay-text">⏳ 正在等待用户扫码支付...</p>
        </div>
      </div>
    </el-card>

    <!-- 支付状态轮询提示 -->
    <el-card v-if="polling" class="status-card" shadow="hover">
      <div class="polling-status">
        <el-icon class="is-loading" :size="18"><Loading /></el-icon>
        <span>正在确认支付结果...</span>
      </div>
    </el-card>
  </div>

  <!-- 加载中 -->
  <div v-else class="loading-area">
    <el-skeleton :rows="6" animated />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { payOrder, queryOrder, mockWechatSuccess } from '@/api/payment'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const qrContent = ref('')
const alipayFormContainer = ref(null)
const polling = ref(false)
const autoPaying = ref(false)
const autoPayProgress = ref(0)
let pollTimer = null
let progressTimer = null

onMounted(async () => {
  const orderNo = route.params.orderNo
  if (!orderNo) {
    ElMessage.error('订单号不存在')
    router.push('/')
    return
  }

  await loadOrder(orderNo)

  // 如果是微信支付，开始自动轮询
  if (order.value && order.value.payType === 'WECHAT' && order.value.status === 'PENDING') {
    startPolling()
    startAutoPaySimulation()
  }

  // 如果是支付宝，提示用户点击支付按钮
  if (order.value && order.value.payType === 'ALIPAY' && order.value.status === 'PENDING') {
    ElMessage.info('请点击「立即支付」按钮跳转支付宝沙箱')
  }
})

onUnmounted(() => {
  stopPolling()
  stopProgress()
})

const loadOrder = async (orderNo) => {
  try {
    const res = await queryOrder(orderNo)
    order.value = res.data
  } catch (error) {
    ElMessage.error('加载订单失败: ' + error.message)
    router.push('/')
  }
}

// ========== 支付宝支付 ==========
const launchAlipay = async () => {
  try {
    const res = await payOrder(order.value.orderNo, 'ALIPAY')
    const data = res.data

    if (data.type === 'ALIPAY' && data.formHtml) {
      // 将支付宝返回的表单 HTML 注入到页面并提交
      const container = alipayFormContainer.value
      container.innerHTML = data.formHtml

      // 自动提交表单（跳转支付宝）
      const form = container.querySelector('form')
      if (form) {
        ElMessage.success('正在跳转到支付宝沙箱...')
        setTimeout(() => {
          form.submit()
        }, 1000)
      } else {
        // 如果没有 form，可能是 JSON 响应，直接显示
        ElMessageBox.alert(data.formHtml, '支付宝响应', { type: 'info' })
      }
    }
  } catch (error) {
    ElMessage.error('发起支付失败: ' + error.message)
  }
}

// ========== 微信支付（Mock） ==========
const mockPayNow = async () => {
  try {
    await mockWechatSuccess(order.value.orderNo)
    ElMessage.success('模拟支付成功！')
    stopPolling()
    stopProgress()
    // 跳转到结果页
    setTimeout(() => {
      router.push({ name: 'PayResult', query: { orderNo: order.value.orderNo, payType: 'WECHAT' } })
    }, 1000)
  } catch (error) {
    ElMessage.error('模拟支付失败: ' + error.message)
  }
}

const startAutoPaySimulation = () => {
  autoPaying.value = true
  let progress = 0
  progressTimer = setInterval(() => {
    progress += Math.random() * 15
    if (progress > 95) progress = 95
    autoPayProgress.value = Math.floor(progress)
  }, 500)
}

const stopProgress = () => {
  autoPaying.value = false
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

// ========== 轮询订单状态 ==========
const startPolling = () => {
  polling.value = true
  pollTimer = setInterval(async () => {
    try {
      const res = await queryOrder(order.value.orderNo)
      const currentStatus = res.data.status

      if (currentStatus === 'PAID') {
        stopPolling()
        stopProgress()
        ElMessage.success('🎉 支付成功！')
        setTimeout(() => {
          router.push({
            name: 'PayResult',
            query: { orderNo: order.value.orderNo, payType: 'WECHAT' }
          })
        }, 1500)
      } else if (currentStatus === 'FAILED' || currentStatus === 'CLOSED') {
        stopPolling()
        stopProgress()
        ElMessage.warning('支付失败或已关闭')
      } else {
        // 更新订单信息
        order.value = res.data
      }
    } catch (error) {
      console.error('轮询失败:', error)
    }
  }, 3000) // 每3秒查询一次
}

const stopPolling = () => {
  polling.value = false
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// ========== 工具方法 ==========
const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.info('复制失败，请手动复制: ' + text)
  }
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', PAID: 'success', FAILED: 'danger', CLOSED: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = { PENDING: '待支付', PAID: '已支付', FAILED: '支付失败', CLOSED: '已关闭' }
  return map[status] || status
}
</script>

<style scoped>
.checkout-view {
  max-width: 700px;
  margin: 0 auto;
}

.order-card, .pay-card, .status-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.order-info {
  padding: 8px 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f7fa;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  width: 100px;
  color: #909399;
  font-size: 14px;
}

.info-row .value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}

.info-row .value.amount {
  font-size: 22px;
  font-weight: 700;
  color: #f56c6c;
}

.alipay-area, .wechat-area {
  text-align: center;
  padding: 20px 0;
}

.alipay-actions, .wechat-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.alipay-form-container {
  margin-top: 20px;
}

.qrcode-area {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.qrcode-placeholder {
  width: 220px;
  height: 220px;
  border: 2px dashed #67c23a;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f0f9eb;
  padding: 20px;
}

.qrcode-placeholder p {
  margin-top: 12px;
  font-size: 14px;
  color: #67c23a;
  font-weight: 600;
}

.qrcode-tip {
  font-size: 11px !important;
  color: #909399 !important;
  font-weight: normal !important;
  word-break: break-all;
  margin-top: 8px !important;
}

.auto-pay-status {
  margin-top: 16px;
  padding: 0 40px;
}

.auto-pay-text {
  margin-top: 8px;
  color: #e6a23c;
  font-size: 14px;
}

.status-card {
  text-align: center;
}

.polling-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #409eff;
  font-size: 14px;
}

.loading-area {
  max-width: 700px;
  margin: 40px auto;
  padding: 20px;
}
</style>
