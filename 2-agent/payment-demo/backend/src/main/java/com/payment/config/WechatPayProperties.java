package com.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性（Mock 模式）
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatPayProperties {

    private String appId;
    private String mchId;
    private String apiV3Key;
    private String certPath;
    private String notifyUrl;
}
