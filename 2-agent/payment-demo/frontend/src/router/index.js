import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/checkout/:orderNo',
    name: 'Checkout',
    component: () => import('@/views/CheckoutView.vue'),
    props: true
  },
  {
    path: '/pay/result',
    name: 'PayResult',
    component: () => import('@/views/PayResultView.vue')
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/OrdersView.vue')
  },
  {
    path: '/alipay/return',
    name: 'AlipayReturn',
    component: () => import('@/views/AlipayReturnView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
