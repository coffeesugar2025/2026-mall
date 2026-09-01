# 支付集成演示项目

## 技术栈
- 前端：Vue 3 + Vite + Element Plus + Axios
- 后端：Spring Boot 3 + MyBatis-Plus + MySQL
- 支付：支付宝沙箱（真实流程）+ 微信支付（Mock 模拟）

## 项目结构
```
payment-demo/
├── frontend/               # Vue3 前端
├── backend/                # Spring Boot 后端
└── README.md
```

## 快速启动

### 后端
1. 修改 `application.yml` 中的数据库连接和支付宝沙箱配置
2. `mvn spring-boot:run`

### 前端
1. `npm install`
2. `npm run dev`

## 支付流程
1. 用户选择商品 → 创建订单
2. 选择支付方式（支付宝 / 微信）
3. 支付宝：跳转沙箱支付页面完成支付
4. 微信：展示 Mock 二维码，模拟扫码支付
5. 后端回调更新订单状态
6. 前端轮询/回调展示支付结果
