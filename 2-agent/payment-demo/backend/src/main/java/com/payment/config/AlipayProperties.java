package com.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String appId;
    private String privateKey;
    private String alipayPublicKey;
    private String gateway;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String format = "JSON";
    private String notifyUrl;
    private String returnUrl;
}
