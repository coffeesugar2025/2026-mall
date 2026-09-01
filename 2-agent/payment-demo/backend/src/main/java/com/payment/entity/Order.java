package com.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("t_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（业务唯一） */
    private String orderNo;

    /** 商品名称 */
    private String productName;

    /** 金额 */
    private BigDecimal amount;

    /** 支付方式：ALIPAY / WECHAT */
    private String payType;

    /** 订单状态：PENDING / PAID / FAILED / CLOSED */
    private String status;

    /** 支付宝/微信交易号 */
    private String tradeNo;

    /** 用户标识 */
    private String userId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
