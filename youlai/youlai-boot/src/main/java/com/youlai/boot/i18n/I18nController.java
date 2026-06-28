package com.youlai.boot.i18n;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class I18nController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/hello")
    public String hello(@RequestParam String name) {
        // 获取当前 Locale（由 LocaleResolver 设置）
        Locale locale = LocaleContextHolder.getLocale();
        // 获取国际化消息，支持参数占位符 {0}, {1}...
        return messageSource.getMessage("greeting1234", new Object[]{name}, locale);
    }

    @GetMapping("/login-text")
    public String loginText() {
        return messageSource.getMessage("user.login", null, LocaleContextHolder.getLocale());
    }
}
