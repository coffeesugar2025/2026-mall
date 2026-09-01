<template>
  <div class="home-view">
    <h2 class="page-title">🛍️ 精选商品</h2>
    <p class="page-desc">选择商品后可使用支付宝沙箱或微信支付（Mock）完成支付</p>

    <div class="product-grid">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        shadow="hover"
      >
        <div class="product-image">
          <img :src="product.image_url" :alt="product.name" />
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-desc">{{ product.description }}</p>
          <div class="product-bottom">
            <span class="product-price">¥{{ product.price.toFixed(2) }}</span>
            <el-button type="primary" size="default" @click="handleBuy(product)">
              立即购买
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 购买对话框 -->
    <el-dialog v-model="buyDialogVisible" title="确认订单" width="500px" :close-on-click-modal="false">
      <div v-if="selectedProduct" class="buy-dialog">
        <div class="buy-product">
          <img :src="selectedProduct.image_url" alt="" class="buy-img" />
          <div>
            <p class="buy-name">{{ selectedProduct.name }}</p>
            <p class="buy-price">¥{{ selectedProduct.price.toFixed(2) }}</p>
          </div>
        </div>

        <el-divider />

        <el-form label-position="top">
          <el-form-item label="选择支付方式">
            <el-radio-group v-model="payType">
              <el-radio-button label="ALIPAY">
                <el-icon><Money /></el-icon> 支付宝（沙箱）
              </el-radio-button>
              <el-radio-button label="WECHAT">
                <el-icon><ChatDotRound /></el-icon> 微信支付（Mock）
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="用户标识（可选）">
            <el-input v-model="userId" placeholder="留空则使用 guest" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="confirmBuy">
          确认下单并支付
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/payment'

const router = useRouter()

// 演示商品数据（与数据库 t_product 对应）
const products = ref([
  { id: 1, name: 'Java编程思想（第4版）', description: 'Java经典教材，适合有一定基础的开发者深入学习和参考', price: 99.00, image_url: 'https://placehold.co/300x300?text=Java+Book' },
  { id: 2, name: '机械键盘 K8 Pro', description: '热插拔机械键盘，RGB背光，蓝牙/有线双模', price: 369.00, image_url: 'https://placehold.co/300x300?text=Keyboard' },
  { id: 3, name: '无线蓝牙耳机', description: '主动降噪，30小时续航，HiFi音质', price: 199.00, image_url: 'https://placehold.co/300x300?text=Earbuds' },
  { id: 4, name: 'Vue3实战开发', description: '从零搭建企业级Vue3+TypeScript项目', price: 79.00, image_url: 'https://placehold.co/300x300?text=Vue3+Book' },
  { id: 5, name: '人体工学椅', description: '腰背分离设计，自适应 lumbar support', price: 1299.00, image_url: 'https://placehold.co/300x300?text=Chair' },
  { id: 6, name: '咖啡机', description: '意式全自动咖啡机，一键萃取', price: 899.00, image_url: 'https://placehold.co/300x300?text=Coffee' },
])

const buyDialogVisible = ref(false)
const selectedProduct = ref(null)
const payType = ref('ALIPAY')
const userId = ref('')
const creating = ref(false)

const handleBuy = (product) => {
  selectedProduct.value = product
  payType.value = 'ALIPAY'
  userId.value = ''
  buyDialogVisible.value = true
}

const confirmBuy = async () => {
  if (!selectedProduct.value) return

  creating.value = true
  try {
    const res = await createOrder({
      productName: selectedProduct.value.name,
      amount: selectedProduct.value.price.toFixed(2),
      payType: payType.value,
      userId: userId.value || 'guest'
    })

    const order = res.data
    ElMessage.success('订单创建成功！')

    buyDialogVisible.value = false

    // 跳转到支付页面
    router.push({
      name: 'Checkout',
      params: { orderNo: order.orderNo },
      query: { payType: payType.value }
    })
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.page-title {
  font-size: 24px;
  margin-bottom: 8px;
  color: #303133;
}

.page-desc {
  color: #909399;
  margin-bottom: 24px;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.product-card {
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-image {
  text-align: center;
  margin-bottom: 12px;
}

.product-image img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}

.product-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  min-height: 40px;
  margin-bottom: 12px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.buy-dialog .buy-product {
  display: flex;
  align-items: center;
  gap: 16px;
}

.buy-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.buy-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.buy-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: 700;
}
</style>
