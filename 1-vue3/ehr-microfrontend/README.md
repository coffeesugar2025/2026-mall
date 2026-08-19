# EHR 微前端平台（ehr-platform + vhr）

基于 Vue 3 + qiankun 构建的企业级人力资源管理系统，采用微前端架构，支持多团队独立开发、独立部署。

---

## 一、技术栈

### 1.1 核心框架与构建

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 前端框架 | Vue | ^3.4.15 | 基于 Composition API |
| 构建工具 | Webpack | ^5.89.0 | 主/子应用统一使用 Webpack 5 |
| 开发语言 | JavaScript (ES6+) | — | 通过 Babel 7 转译 |
| 微前端框架 | qiankun | 2.10.14 | 主子应用调度与沙箱隔离 |
| CSS 预处理器 | Less | ^4.1.3 | 样式开发 |
| CSS 后处理 | PostCSS + autoprefixer | ^8.4.31 / ^10.4.13 | 浏览器兼容 |

### 1.2 路由与状态管理

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue Router | ^4.1.6 | 主/子应用各自独立路由 |
| Vuex | ^4.1.0 | 主应用全局状态（用户、Token、权限） |

### 1.3 UI 组件库

| 技术 | 版本 | 说明 |
|------|------|------|
| Element Plus | ^2.2.27 | 核心 UI 组件库 |
| @element-plus/icons-vue | ^2.0.10 | 图标（Vue 3 版） |
| @element-plus/icons | ^0.0.11 | 旧版图标（兼容） |

### 1.4 主应用依赖（ehr-platform）

| 类别 | 技术 | 版本 |
|------|------|------|
| HTTP 请求 | axios | ^1.2.2 |
| 微前端 | qiankun | 2.10.14 |
| 开发服务器 | webpack-dev-server | ^4.11.1 |
| 代码分割/合并 | webpack-merge | ^5.8.0 |
| HTML 模板 | html-webpack-plugin | ^5.5.0 |

### 1.5 子应用依赖（vhr 员工管理模块）

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 核心框架 | vue, vue-router, element-plus | 同主应用 | — |
| 数据可视化 | echarts | ^5.4.2 | 图表（人事数据看板） |
| 加密/安全 | crypto-js, crypto-browserify, node-forge | ^4.1.1 / ^3.12.0 / ^1.3.1 | 多方案加密 |
| 工具库 | lodash | ^4.17.21 | 通用工具函数 |
| Office 文档 | js-pageoffice | 6.5.1 | 在线预览/编辑 Office 文件 |
| 瀑布流 | vue-waterfall-plugin-next | ^2.6.5 | 图片/卡片瀑布流布局 |
| 浏览器兼容 | css-vars-ponyfill, core-js | ^2.4.8 / ^3.30.1 | CSS 变量 + ES6+ Polyfill |
| Node Polyfill | node-polyfill-webpack-plugin | ^3.0.0 | Node 核心模块浏览器端兼容 |
| jQuery | jquery | ^3.6.3 | 旧代码/插件兼容 |

### 1.6 工程化工具链

| 类别 | 技术 | 说明 |
|------|------|------|
| 代码检查 | ESLint + eslint-plugin-vue + @babel/eslint-parser | Vue/JS 规范 |
| 代码格式化 | Prettier + eslint-plugin-prettier | 统一风格 |
| 编译加速 | esbuild-loader + thread-loader | 多线程 + esbuild 转译 |
| 包分析 | webpack-bundle-analyzer + speed-measure-webpack-plugin | 体积与速度分析 |
| 样式优化 | mini-css-extract-plugin + purgecss-webpack-plugin | CSS 分离与无用清除 |
| 压缩 | terser-webpack-plugin + css-minimizer-webpack-plugin | JS/CSS 生产压缩 |
| Gzip | compression-webpack-plugin | 服务端 Gzip 预压缩 |
| 自动导入 | unplugin-auto-import + unplugin-vue-components + unplugin-element-plus | API/组件按需引入 |
| 环境管理 | dotenv + cross-env | 多环境配置 |

---

## 二、系统功能

### 2.1 主应用（基座 ehr-platform）

| 模块 | 功能说明 |
|------|----------|
| **统一登录** | JWT Token 鉴权，登录态全局共享 |
| **工作台** | 在职人数、本月入职/离职、待审批等核心指标卡片 |
| **统一导航** | 侧边栏菜单聚合所有子应用入口，路由自动分发 |
| **用户状态管理** | Vuex 存储用户信息、Token，通过 props 注入子应用 |
| **微应用调度** | qiankun 负责子应用加载、卸载、沙箱隔离、预加载 |
| **全局状态通信** | initGlobalState 实现主→子、子→主双向状态同步 |
| **样式隔离** | 实验性样式隔离（experimentalStyleIsolation）防止子应用样式污染 |

### 2.2 子应用（vhr 员工管理模块）

| 模块 | 功能说明 |
|------|----------|
| **员工列表** | 分页表格展示，支持按姓名/部门搜索，状态标签（在职/离职） |
| **员工档案** | 详情页展示个人信息、岗位、入职日期、联系方式等 |
| **新增员工** | 弹窗表单录入新员工信息 |
| **编辑员工** | 修改已有员工数据 |
| **离职处理** | 一键标记离职，二次确认防误触 |
| **路由守卫** | 子应用独立路由，支持浏览器前进/后退/刷新 |
| **主子通信** | 通过 props 接收主应用 Token，自动注入 axios 请求头 |

### 2.3 微前端架构能力

| 能力 | 说明 |
|------|------|
| **独立开发部署** | 各子应用可独立 `npm run start` / `npm run build` |
| **技术栈无关** | 新子应用可选用不同框架（React/Angular）接入 |
| **按需加载** | 访问对应菜单才加载子应用资源 |
| **预加载** | `prefetch: 'all'` 空闲时预拉取子应用静态资源 |
| **沙箱隔离** | JS 沙箱 + CSS 样式隔离，子应用互不影响 |
| **本地组件复用** | 通过 `vhr@file:../../ui-components` 引用私有组件库 |
| **多环境支持** | dev / testing / prod 三套环境配置 |

---

## 三、项目结构

\`\`\`
ehr-microfrontend/
├── ehr-platform/              # 主应用（基座）
│   ├── src/
│   │   ├── main.js            # 入口 + qiankun 启动
│   │   ├── App.vue
│   │   ├── router/            # 主应用路由
│   │   ├── store/             # Vuex 状态
│   │   ├── layouts/           # MainLayout（含 #subapp-viewport）
│   │   ├── views/             # Dashboard、Login
│   │   └── micro/             # 子应用注册配置
│   └── build/                 # Webpack 配置
│
├── vhr/                       # 子应用（员工管理）
│   ├── src/
│   │   ├── main.js            # 入口 + 生命周期导出
│   │   ├── public-path.js     # qiankun publicPath 修正
│   │   ├── App.vue
│   │   ├── router/            # 子应用路由
│   │   └── views/             # EmployeeList、EmployeeDetail
│   └── build/                 # Webpack 配置
│
└── ui-components/             # 私有组件库（本地引用）
\`\`\`

---

## 四、快速启动

\`\`\`bash
# 主应用
cd ehr-platform && npm install && npm run start
# → http://localhost:8080

# 子应用（另开终端）
cd vhr && npm install && npm run start
# → http://localhost:8081

# 访问主应用，点击「员工管理」→「员工列表」
\`\`\`

---

## 五、后续可扩展子应用

| 子应用 | 说明 |
|--------|------|
| attendance | 考勤管理（打卡、请假、加班） |
| payroll | 薪酬管理（工资条、社保、个税） |
| performance | 绩效管理（考核、评分、目标） |
| recruitment | 招聘管理（简历、面试、Offer） |
\`\`\`