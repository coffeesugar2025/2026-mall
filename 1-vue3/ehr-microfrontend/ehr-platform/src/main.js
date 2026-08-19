import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { registerMicroApps, start } from 'qiankun';

const app = createApp(App);
app.use(router);
app.use(store);
app.use(ElementPlus);

// 先挂载主应用
app.mount('#app');

// 🔥 关键修复：等路由就绪 + DOM 渲染完成后再注册和启动 qiankun
router.isReady().then(() => {
  registerMicroApps([
    {
      name: 'vhr',
      entry: process.env.NODE_ENV === 'development'
        ? '//localhost:8081'
        : '/vhr/',
      container: '#subapp-viewport',
      activeRule: '/vhr',
      props: {
        base: '/vhr',
        getToken: () => store.state.token,
        userInfo: store.state.user,
      },
    },
  ]);

  if (!window.__QIANKUN_STARTED__) {
    window.__QIANKUN_STARTED__ = true;
    start({
      prefetch: 'all',
      sandbox: { experimentalStyleIsolation: true },
      singular: true,
    });
    console.log('[主应用] qiankun started');
  }
});
