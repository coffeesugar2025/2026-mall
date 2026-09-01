# 微信支付 Mock 说明

## 概述

由于微信支付需要企业资质才能开通，本项目使用 **Mock 模式** 完整模拟微信支付流程：

```
用户选择微信支付 → 创建订单 → 展示"二维码" → 模拟扫码 → 异步回调 → 订单状态更新
```

## Mock 流程说明

### 前端流程
1. 用户点击「微信支付」
2. 前端展示模拟二维码区域
3. 启动 3 秒轮询，查询订单状态
4. 后端 3~8 秒后自动触发 Mock 回调
5. 前端轮询检测到 `PAID` 状态，跳转结果页

### 后端流程
1. `POST /api/pay/pay/{orderNo}?payType=WECHAT`
2. 生成 Mock `prepay_id` 和二维码内容
3. 返回给前端展示
4. `@Async` 异步线程 3~8 秒后自动调用 `handleWechatNotify`
5. 更新订单状态为 `PAID`

### 手动触发
前端提供「模拟立即支付成功」按钮，可跳过等待：
```
POST /api/pay/wechat/mock-success/{orderNo}
```

## 如何替换为真实微信支付

当有了真实商户号后，修改以下文件：

### 1. `WechatPayConfig.java`
- 加载真实证书
- 配置 HTTP 客户端（需要证书认证）

### 2. `OrderServiceImpl.createWechatPayment()`
- 替换为调用微信支付 V3 API
- 获取真实 `code_url` 用于生成二维码
- 返回真实 JSAPI 参数

### 3. `OrderServiceImpl.handleWechatNotify()`
- 添加签名验证
- 解密微信回调报文
- 验证金额一致性

### 4. 新增工具类
- `WechatPaySignature` - 签名生成与验证
- `WechatPayHttpClient` - 带证书的 HTTP 请求
- `WechatNotifyDecrypt` - 回调报文解密

## 微信支付 V3 API 核心接口

| 接口 | 用途 | 文档 |
|------|------|------|
| `/v3/pay/transactions/native` | Native 扫码支付 | [官方文档](https://pay.weixin.qq.com/doc/v3/merchant/401207/401269/401303) |
| `/v3/pay/transactions/jsapi` | JSAPI 公众号支付 | - |
| `/v3/pay/transactions/out-trade-no/{out_trade_no}/close` | 关闭订单 | - |
| `/v3/pay/transactions/out-trade-no/{out_trade_no}` | 查询订单 | - |

## 生产环境注意事项

1. **必须验证签名** - 防止伪造回调
2. **必须验证金额** - 防止金额篡改
3. **幂等处理** - 同一订单多次回调只处理一次
4. **超时关闭** - 定时任务关闭超时未支付订单
5. **证书管理** - 使用证书热更新机制
6. **日志审计** - 记录所有支付操作日志
