import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录' },
      },
    ],
  },
  // 🔥 关键修复：/vhr/* 全部匹配到 MainLayout，确保 #subapp-viewport 存在
  {
    path: '/vhr/:pathMatch(.*)*',
    component: () => import('@/layouts/MainLayout.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
