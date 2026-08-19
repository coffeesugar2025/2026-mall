// vhr/src/main.js
import './public-path';
import { createApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import axios from 'axios';

// ========== 全局变量 ==========
let app = null;
let routerInstance = null;

// ========== 路由表 ==========
const routes = [
  {
    path: '/',
    redirect: '/employee/list',
  },
  {
    path: '/employee/list',
    name: 'EmployeeList',
    component: () => import('@/views/EmployeeList.vue'),
    meta: { title: '员工列表' },
  },
  {
    path: '/employee/detail/:id',
    name: 'EmployeeDetail',
    component: () => import('@/views/EmployeeDetail.vue'),
    meta: { title: '员工档案' },
  },
];

// ========== 渲染函数 ==========
function render(props = {}) {
  const { container, base = '/vhr', getToken } = props;

  // token 注入
  if (getToken) {
    axios.defaults.headers.common['Authorization'] = `Bearer ${getToken()}`;
  }

  // ✅ base 处理：qiankun 环境用 /vhr，独立运行用 /
  const finalBase = window.__POWERED_BY_QIANKUN__ ? base : '/';

  routerInstance = createRouter({
    history: createWebHistory(finalBase),
    routes,
  });

  app = createApp(App);
  app.use(routerInstance);
  app.use(ElementPlus);

  // 挂载
  const mountDOM = container
    ? container.querySelector('#app')
    : document.querySelector('#app');

  app.mount(mountDOM);
}

// ========== qiankun 生命周期 ==========
export async function bootstrap() {
  console.log('[vhr] bootstrap');
}

export async function mount(props) {
  console.log('[vhr] mount', props);
  window.vhrProps = props;
  render(props);
}

export async function unmount() {
  console.log('[vhr] unmount');
  if (app) {
    app.unmount();
    app = null;
  }
  routerInstance = null;
}

export async function update(props) {
  console.log('[vhr] update', props);
}

// ========== 独立运行 ==========
if (!window.__POWERED_BY_QIANKUN__) {
  render({ base: '/' });
}