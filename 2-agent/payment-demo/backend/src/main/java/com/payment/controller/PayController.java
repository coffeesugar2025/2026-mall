package com.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.payment.common.ApiResponse;
import com.payment.config.AlipayProperties;
import com.payment.entity.Order;
import com.payment.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付接口控制器
 */
@RestController
@RequestMapping("/api/pay")
@Slf4j
public class PayController {

    private final OrderService orderService;
    private final AlipayProperties alipayProperties;

    public PayController(OrderService orderService, AlipayProperties alipayProperties) {
        this.orderService = orderService;
        this.alipayProperties = alipayProperties;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create-order")
    public ApiResponse<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.getProductName(),
                new BigDecimal(request.getAmount()),
                request.getPayType(),
                request.getUserId()
        );
        return ApiResponse.success(order);
    }

    /**
     * 发起支付
     */
    @PostMapping("/pay/{orderNo}")
    public ApiResponse<?> pay(@PathVariable String orderNo, @RequestParam String payType) {
        if ("ALIPAY".equalsIgnoreCase(payType)) {
            String formHtml = orderService.createAlipayPayment(orderNo);
            return ApiResponse.success(Map.of("type", "ALIPAY", "formHtml", formHtml));
        } else if ("WECHAT".equalsIgnoreCase(payType)) {
            Map<String, Object> result = orderService.createWechatPayment(orderNo);
            return ApiResponse.success(Map.of("type", "WECHAT", "data", result));
        }
        return ApiResponse.error(400, "不支持的支付方式");
    }

    /**
     * 支付宝回调通知
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        try {
            orderService.handleAlipayNotify(params);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    /**
     * 支付宝同步回调（前端跳转）
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) {
        // 前端页面自行轮询订单状态，这里直接重定向
        try {
            log.info("params {}", JSONObject.toJSONString(params));
            String orderNo = params.get("out_trade_no");
            response.sendRedirect("http://localhost:5173/pay/result?orderNo=" + orderNo + "&payType=ALIPAY");
        } catch (Exception e) {
            try {
                response.sendRedirect("http://localhost:5173/pay/result?error=alipay_return_failed");
            } catch (Exception ignored) {}
        }
    }

    /**
     * 微信支付回调通知（Mock）
     */
    @PostMapping("/wechat/notify")
    public String wechatNotify(@RequestBody Map<String, String> params) {
        try {
            orderService.handleWechatNotify(params);
            return "{\"code\": \"SUCCESS\", \"message\": \"OK\"}";
        } catch (Exception e) {
            return "{\"code\": \"FAIL\", \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    /**
     * 手动触发微信支付成功（Mock 调试用）
     */
    @PostMapping("/wechat/mock-success/{orderNo}")
    public ApiResponse<?> mockWechatSuccess(@PathVariable String orderNo) {
        orderService.mockWechatPaySuccess(orderNo);
        return ApiResponse.success("Mock 支付成功");
    }

    /**
     * 查询订单状态
     */
    @GetMapping("/order/{orderNo}")
    public ApiResponse<Order> queryOrder(@PathVariable String orderNo) {
        log.info("orderNo {}", orderNo);
        return ApiResponse.success(orderService.getByOrderNo(orderNo));
    }

    /**
     * 关闭订单
     */
    @PostMapping("/order/{orderNo}/close")
    public ApiResponse<?> closeOrder(@PathVariable String orderNo) {
        orderService.closeOrder(orderNo);
        return ApiResponse.success("订单已关闭");
    }

    /**
     * 获取支付宝配置（前端需要 returnUrl 等）
     */
    @GetMapping("/alipay/config")
    public ApiResponse<?> getAlipayConfig() {
        return ApiResponse.success(Map.of(
                "returnUrl", alipayProperties.getReturnUrl()
        ));
    }

    // ========== 内部 DTO ==========

    public static class CreateOrderRequest {
        private String productName;
        private String amount;
        private String payType; // ALIPAY / WECHAT
        private String userId;

        // Getters & Setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getPayType() { return payType; }
        public void setPayType(String payType) { this.payType = payType; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
}
