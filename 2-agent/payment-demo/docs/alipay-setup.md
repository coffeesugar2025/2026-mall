# 支付宝沙箱环境配置指南

## 1. 注册支付宝开放平台

访问 https://open.alipay.com 并使用支付宝账号登录

## 2. 进入沙箱环境

1. 登录后点击左侧菜单 **「开发助手」→「沙箱」**
2. 系统会自动创建一个沙箱应用

## 3. 获取配置信息

在沙箱管理页面，你需要记录以下信息：

| 配置项 | 位置 | 填入 application.yml 的字段 |
|--------|------|------------------------------|
| APPID | 沙箱应用信息 | `alipay.app-id` |
| 商户私钥 | 点击「查看」→ 复制私钥 | `alipay.private-key` |
| 支付宝公钥 | 沙箱信息页面 | `alipay.alipay-public-key` |
| 网关地址 | 沙箱信息页面 | `alipay.gateway` |

## 4. 配置 application.yml

```yaml
alipay:
  app-id: 2021000000000000          # 你的沙箱APPID
  private-key: MIIEvQIBADANBg...     # 你的商户私钥（完整内容）
  alipay-public-key: MIIBIjANBg...   # 你的支付宝公钥（完整内容）
  gateway: https://openapi-sandbox.dl.alipaydev.com/gateway.do
  notify-url: http://your-domain.ngrok.io/api/alipay/notify
  return-url: http://localhost:5173/alipay/return
```

## 5. 关于回调地址（notify-url）

沙箱环境需要公网可访问的回调地址：

### 方案一：使用 ngrok（推荐）
```bash
# 安装 ngrok: https://ngrok.com
ngrok http 8080
# 将生成的 https 地址填入 notify-url
# 例如: https://xxxx.ngrok.io/api/alipay/notify
```

### 方案二：使用 frp 内网穿透
### 方案三：本地测试时忽略回调（手动触发）

## 6. 下载沙箱版支付宝 APP

在沙箱页面下载 **「支付宝沙箱版」** APP（Android），用于扫码支付测试。

## 7. 测试流程

1. 启动后端 + 前端
2. 选择商品 → 选择支付宝支付
3. 点击「立即支付」→ 跳转沙箱支付页面
4. 使用沙箱账号登录并支付
5. 支付完成后自动跳转回前端页面
6. 后端收到异步回调，更新订单状态

## 常见问题

### Q: 提示「应用不存在」？
A: 检查 `app-id` 是否正确，确保使用的是沙箱 APPID

### Q: 提示「签名错误」？
A: 检查私钥和支付宝公钥是否完整复制，注意换行符

### Q: 回调收不到？
A: 确认 `notify-url` 是公网可访问的 HTTPS 地址
