package com.salary.calcengine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;


@ComponentScan({"com.salary.**"})
@Slf4j
@SpringBootApplication
public class CalcEngineApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(CalcEngineApplication.class);
        springApplication.setAllowBeanDefinitionOverriding(true);
        ConfigurableApplicationContext context = springApplication.run(args);
        final ConfigurableEnvironment environment = context.getEnvironment();
        final String port = environment.getProperty("server.port");
        final String contextPath = environment.getProperty("server.servlet.context-path");
        log.info("项目启动成功，访问地址：http://**:" + port + contextPath);
    }

}
