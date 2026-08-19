# 子应用接入规范（qiankun 微前端）

本文档定义新子应用接入 EHR 微前端平台的**强制规范**，所有子应用必须遵循。

---

## 一、命名规范

| 项目 | 规范 | 示例 |
|------|------|------|
| 子应用 name | 小写字母 + 连字符，全局唯一 | `attendance`、`payroll` |
| 路由前缀 | 与 name 一致 | `/attendance` |
| Nginx location | 与 name 一致 | `/attendance/` |
| package.json name | 与子应用 name 一致 | `"name": "attendance"` |

---

## 二、目录结构

\`\`\`
subapp-name/
├── src/
│   ├── public-path.js        # ✅ 必须：qiankun publicPath 修正
│   ├── main.js              # ✅ 必须：导出 bootstrap/mount/unmount
│   ├── App.vue
│   ├── router/
│   │   └── index.js         # 路由 base 从 props 读取
│   ├── views/
│   └── styles/
├── build/
│   ├── webpack.base.conf.js  # ✅ 必须：output.library = 子应用 name
│   ├── webpack.dev.conf.js
│   └── webpack.prod.conf.js  # ✅ 必须：output.publicPath = /name/
├── public/
│   └── index.html           # 挂载点 ID = app
└── package.json
\`\`\`

---

## 三、必须实现的代码规范

### 3.1 public-path.js（必须存在）

\`\`\`js
if (window.__POWERED_BY_QIANKUN__) {
  // eslint-disable-next-line no-undef
  __webpack_public_path__ = window.__INJECTED_PUBLIC_PATH_BY_QIANKUN__;
}
\`\`\`

### 3.2 main.js 模板

\`\`\`js
import './public-path';
import { createApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

let app = null;
let routerInstance = null;

const routes = [
  // 所有 path 不要带 /name 前缀，base 由 createWebHistory 统一处理
  { path: '/', redirect: '/list' },
  { path: '/list', component: () => import('@/views/List.vue') },
  { path: '/detail/:id', component: () => import('@/views/Detail.vue') },
];

function render(props = {}) {
  const { container, base = '/subapp-name', getToken } = props;

  if (getToken) {
    // 注入 token 到请求头
  }

  const finalBase = window.__POWERED_BY_QIANKUN__ ? base : '/';

  routerInstance = createRouter({
    history: createWebHistory(finalBase),
    routes,
  });

  app = createApp(App);
  app.use(routerInstance);
  app.use(ElementPlus);

  const mountDOM = container
    ? container.querySelector('#app')
    : document.querySelector('#app');

  app.mount(mountDOM);
}

export async function bootstrap() {}
export async function mount(props) { window.subappProps = props; render(props); }
export async function unmount() {
  if (app) { app.unmount(); app = null; }
  routerInstance = null;
}
export async function update(props) {}

if (!window.__POWERED_BY_QIANKUN__) {
  render({ base: '/' });
}
\`\`\`

### 3.3 Webpack 输出配置（必须）

\`\`\`js
// build/webpack.base.conf.js
output: {
  library: 'subapp-name',           // ← 必须和 package.json name 一致
  libraryTarget: 'umd',
  chunkLoadingGlobal: 'webpackJsonp_subapp_name',
  clean: true,
}
\`\`\`

### 3.4 生产环境 publicPath

\`\`\`js
// build/webpack.prod.conf.js
output: {
  publicPath: '/subapp-name/',     // ← 必须和 Nginx location 一致
}
\`\`\`

---

## 四、路由规范

| 规则 | 正确 ✅ | 错误 ❌ |
|------|---------|---------|
| 路由 path 带前缀 | `/list` | `/subapp-name/list` |
| 内部跳转 | `router.push('/detail/1')` | `router.push('/subapp-name/detail/1')` |
| 路由 base | 从 props.base 读取 | 硬编码 `/subapp-name` |
| 菜单 index（主应用侧） | `/subapp-name/list` | `http://localhost:xxx/list` |

---

## 五、通信规范

### 5.1 主 → 子（通过 props）

\`\`\`js
// 主应用注册时
props: {
  base: '/subapp-name',
  getToken: () => store.state.token,
  userInfo: store.state.user,
}
\`\`\`

### 5.2 子 → 主（通过 initGlobalState）

\`\`\`js
// 子应用内
const { onGlobalStateChange } = window.subappProps;
onGlobalStateChange((state) => {
  console.log('主应用状态变更:', state);
}, true);
\`\`\`

### 5.3 禁止事项

- ❌ 子应用直接 `import` 主应用代码
- ❌ 子应用直接修改主应用 DOM
- ❌ 子应用间直接互相调用（通过主应用中转）

---

## 六、样式规范

| 规则 | 说明 |
|------|------|
| 使用 scoped 或 CSS Modules | 防止全局污染 |
| 不写 `body { ... }` 全局样式 | 影响主应用和其他子应用 |
| 不覆盖 Element Plus 全局变量 | 除非主应用统一约定 |
| 主应用开启 `experimentalStyleIsolation` | 子应用样式自动加前缀 |

---

## 七、接入流程 Checklist

\`\`\`markdown
- [ ] 1. 主应用 `registerMicroApps` 添加子应用配置
- [ ] 2. 主应用侧边栏菜单添加入口（index = `/subapp-name/xxx`）
- [ ] 3. 主应用路由添加兜底：`{ path: '/subapp-name/:pathMatch(.*)*', component: MainLayout }`
- [ ] 4. 子应用 `package.json` name 唯一
- [ ] 5. 子应用 `public-path.js` 存在
- [ ] 6. 子应用 `main.js` 导出 qiankun 生命周期
- [ ] 7. 子应用 webpack `output.library` = name
- [ ] 8. 子应用生产构建 `publicPath` = `/subapp-name/`
- [ ] 9. Nginx 添加 `location /subapp-name` 指向子应用 dist
- [ ] 10. 本地联调：主+子同时启动，验证挂载/卸载/刷新/前进后退
- [ ] 11. 生产部署：验证静态资源路径、API 代理、样式隔离
\`\`\`

---

## 八、常见问题速查

| 现象 | 原因 | 解决 |
|------|------|------|
| `#subapp-viewport not existed` | 容器 DOM 未渲染就 start | 等 `router.isReady()` 后再 `start()` |
| `router is not defined` | 子应用 main.js 未声明 router 变量 | 顶部 `let routerInstance = null` |
| 地址栏出现 `localhost:8081/vhr/vhr/...` | base 重复拼接 | 子应用路由 path 不带前缀 + base 只传一次 |
| 子应用样式污染主应用 | 未开启样式隔离 | 主应用 `sandbox: { experimentalStyleIsolation: true }` |
| 刷新 404 | Nginx 未配 try_files | `try_files $uri $uri/ /subapp-name/index.html` |
| 静态资源 404 | publicPath 不对 | 生产 `publicPath: '/subapp-name/'` |
\`\`\`