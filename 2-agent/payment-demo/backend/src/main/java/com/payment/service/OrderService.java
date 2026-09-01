package com.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.payment.entity.Order;

import java.util.Map;
import java.math.BigDecimal;

public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(String productName, BigDecimal amount, String payType, String userId);

    /**
     * 根据订单号查询
     */
    Order getByOrderNo(String orderNo);

    /**
     * 发起支付宝支付，返回支付表单HTML
     */
    String createAlipayPayment(String orderNo);

    /**
     * 发起微信支付（Mock），返回二维码链接等信息
     */
    Map<String, Object> createWechatPayment(String orderNo);

    /**
     * 支付宝异步回调处理
     */
    void handleAlipayNotify(Map<String, String> params);

    /**
     * 微信支付异步回调处理（Mock）
     */
    void handleWechatNotify(Map<String, String> params);

    /**
     * 模拟微信扫码支付成功（Mock 专用）
     */
    void mockWechatPaySuccess(String orderNo);

    /**
     * 分页查询订单
     */
    Page<Order> pageOrders(int page, int size, String status);

    /**
     * 关闭订单
     */
    void closeOrder(String orderNo);
}
