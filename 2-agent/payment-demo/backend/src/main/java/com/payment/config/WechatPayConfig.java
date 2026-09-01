package com.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 微信支付配置（Mock 模式）
 * 生产环境应使用官方 SDK
 */
@Configuration
public class WechatPayConfig {

    private final WechatPayProperties wechatPayProperties;

    public WechatPayConfig(WechatPayProperties wechatPayProperties) {
        this.wechatPayProperties = wechatPayProperties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public WechatPayProperties getWechatPayProperties() {
        return wechatPayProperties;
    }
}
