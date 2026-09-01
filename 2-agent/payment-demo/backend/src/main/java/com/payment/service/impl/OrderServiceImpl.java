package com.payment.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.BusinessException;
import com.payment.config.AlipayProperties;
import com.payment.config.WechatPayProperties;
import com.payment.entity.Order;
import com.payment.mapper.OrderMapper;
import com.payment.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;
    private final WechatPayProperties wechatPayProperties;
    private final ObjectMapper objectMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                           AlipayClient alipayClient,
                           AlipayProperties alipayProperties,
                           WechatPayProperties wechatPayProperties,
                           ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.alipayClient = alipayClient;
        this.alipayProperties = alipayProperties;
        this.wechatPayProperties = wechatPayProperties;
        this.objectMapper = objectMapper;
    }

    // ==================== 创建订单 ====================

    @Override
    @Transactional
    public Order createOrder(String productName, BigDecimal amount, String payType, String userId) {
        if (!StringUtils.hasText(productName)) {
            throw new BusinessException("商品名称不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        if (!"ALIPAY".equals(payType) && !"WECHAT".equals(payType)) {
            throw new BusinessException("支付方式不支持: " + payType);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductName(productName);
        order.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        order.setPayType(payType);
        order.setStatus("PENDING");
        order.setUserId(userId != null ? userId : "guest");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        orderMapper.insert(order);
        log.info("创建订单成功: orderNo={}, amount={}, payType={}", order.getOrderNo(), amount, payType);
        return order;
    }

    // ==================== 支付宝支付 ====================

    @Override
    public String createAlipayPayment(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态异常: " + order.getStatus());
        }

        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl()+orderNo);

            // 构建业务参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", order.getOrderNo());
            bizContent.put("total_amount", order.getAmount().toPlainString());
            bizContent.put("subject", order.getProductName());
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            // 可选：超时时间
            bizContent.put("timeout_express", "30m");

            request.setBizContent(objectMapper.writeValueAsString(bizContent));

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                log.info("支付宝下单成功: orderNo={}", orderNo);
                return response.getBody(); // 返回HTML表单
            } else {
                log.error("支付宝下单失败: code={}, msg={}", response.getCode(), response.getMsg());
                throw new BusinessException("支付宝下单失败: " + response.getMsg());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝支付异常", e);
            throw new BusinessException("支付宝支付异常: " + e.getMessage());
        }
    }

    // ==================== 微信支付（Mock） ====================

    @Override
    public Map<String, Object> createWechatPayment(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态异常: " + order.getStatus());
        }

        // Mock 模式：生成模拟的预支付交易信息
        String mockPrepayId = "mock_prepay_" + UUID.randomUUID().toString().replace("-", "");
        String mockCodeUrl = "https://mock.wechat.pay/qr/" + mockPrepayId;

        // 构建 Mock 的 JSAPI 调起支付参数
        Map<String, String> jsapiParams = new HashMap<>();
        jsapiParams.put("appId", wechatPayProperties.getAppId());
        jsapiParams.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        jsapiParams.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        jsapiParams.put("package", "prepay_id=" + mockPrepayId);
        jsapiParams.put("signType", "RSA");
        jsapiParams.put("paySign", generateMockSignature());

        // 模拟生成二维码内容（实际项目中是 code_url）
        String qrContent = "weixin://wxpay/bizpayurl?pr=" + mockPrepayId;

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("amount", order.getAmount());
        result.put("prepayId", mockPrepayId);
        result.put("codeUrl", mockCodeUrl);
        result.put("qrContent", qrContent);
        result.put("jsapiParams", jsapiParams);
        result.put("mockNotice", "⚠️ 当前为 Mock 模式，非真实微信支付");

        log.info("微信支付(Mock)下单成功: orderNo={}, prepayId={}", orderNo, mockPrepayId);

        // 异步模拟用户扫码支付（5秒后自动回调）
        simulateUserScanPayment(orderNo, mockPrepayId);

        return result;
    }

    /**
     * 模拟用户扫码支付（Mock）
     * 5秒后自动触发支付成功回调
     */
    @Async("mockPayExecutor")
    public void simulateUserScanPayment(String orderNo, String prepayId) {
        try {
            // 模拟用户扫码 + 确认支付耗时
            int delaySeconds = ThreadLocalRandom.current().nextInt(3, 8);
            Thread.sleep(delaySeconds * 1000L);

            // 构建模拟的微信支付回调参数
            Map<String, String> notifyParams = new HashMap<>();
            notifyParams.put("out_trade_no", orderNo);
            notifyParams.put("transaction_id", "MOCK_" + System.currentTimeMillis());
            notifyParams.put("trade_state", "SUCCESS");
            notifyParams.put("total_amount", getByOrderNo(orderNo).getAmount().multiply(new BigDecimal(100)).toPlainString());
            notifyParams.put("result_code", "SUCCESS");
            notifyParams.put("return_code", "SUCCESS");
            notifyParams.put("time_end", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            handleWechatNotify(notifyParams);
            log.info("Mock 模拟用户支付完成: orderNo={}, delay={}s", orderNo, delaySeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Mock 模拟支付异常: orderNo={}", orderNo, e);
        }
    }

    // ==================== 支付宝回调 ====================

    @Override
    @Transactional
    public void handleAlipayNotify(Map<String, String> params) {
        log.info("收到支付宝回调: {}", params);

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        // 注意：生产环境需要验签！
        // boolean verified = alipaySignatureService.verify(params);

        Order order = getByOrderNo(outTradeNo);
        if (!"PENDING".equals(order.getStatus())) {
            log.warn("订单已处理，忽略重复通知: orderNo={}", outTradeNo);
            return;
        }

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            order.setStatus("PAID");
            order.setTradeNo(tradeNo);
            order.setPayTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("支付宝支付成功: orderNo={}, tradeNo={}", outTradeNo, tradeNo);
        } else if ("TRADE_CLOSED".equals(tradeStatus)) {
            order.setStatus("CLOSED");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("支付宝订单关闭: orderNo={}", outTradeNo);
        }
    }

    // ==================== 微信回调（Mock） ====================

    @Override
    @Transactional
    public void handleWechatNotify(Map<String, String> params) {
        log.info("收到微信支付回调(Mock): {}", params);

        String outTradeNo = params.get("out_trade_no");
        String transactionId = params.get("transaction_id");
        String tradeState = params.get("trade_state");

        Order order = getByOrderNo(outTradeNo);
        if (!"PENDING".equals(order.getStatus())) {
            log.warn("订单已处理，忽略重复通知: orderNo={}", outTradeNo);
            return;
        }

        if ("SUCCESS".equals(tradeState)) {
            order.setStatus("PAID");
            order.setTradeNo(transactionId);
            order.setPayTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("微信支付成功(Mock): orderNo={}, transactionId={}", outTradeNo, transactionId);
        } else {
            order.setStatus("FAILED");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("微信支付失败(Mock): orderNo={}, state={}", outTradeNo, tradeState);
        }
    }

    // ==================== 手动触发 Mock 微信支付成功 ====================

    @Override
    @Transactional
    public void mockWechatPaySuccess(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态异常: " + order.getStatus());
        }

        order.setStatus("PAID");
        order.setTradeNo("MOCK_MANUAL_" + System.currentTimeMillis());
        order.setPayTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("手动触发 Mock 微信支付成功: orderNo={}", orderNo);
    }

    // ==================== 查询 ====================

    @Override
    public Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BusinessException("订单不存在: " + orderNo);
        }
        return order;
    }

    @Override
    public Page<Order> pageOrders(int page, int size, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void closeOrder(String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待支付订单才能关闭");
        }
        order.setStatus("CLOSED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单已关闭: orderNo={}", orderNo);
    }

    // ==================== 工具方法 ====================

    private String generateOrderNo() {
        // 格式：年月日时分秒 + 6位随机数
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + random;
    }

    private String generateMockSignature() {
        return "MOCK_SIGN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
