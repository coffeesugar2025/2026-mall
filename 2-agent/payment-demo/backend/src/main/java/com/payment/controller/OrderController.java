package com.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.payment.common.ApiResponse;
import com.payment.entity.Order;
import com.payment.service.OrderService;
import org.springframework.web.bind.annotation.*;

/**
 * 订单查询接口
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 分页查询订单
     */
    @GetMapping
    public ApiResponse<Page<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.pageOrders(page, size, status));
    }

    /**
     * 查询单个订单
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> get(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getByOrderNo(orderNo));
    }
}
