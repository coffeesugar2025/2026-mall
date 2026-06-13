
package com.policy.web;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * <p>
 * 启动优化参数:
 * -server -Xms4096m -Xmx4096m  -Xss1024k -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC
 * 说明:
 * -XX:+UseParallelOldGC:配置老年代垃圾收集器为并行收集。JDK6.0支持对老年代并行收集。
 * SurvivorRatio:年轻代中Eden区与两个Survivor区的比值
 * <p>
 */
@EnableAsync
@EnableScheduling
@EnableCaching(order = -2)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, SecurityAutoConfiguration.class, RabbitAutoConfiguration.class}, scanBasePackages = "com.policy")
@MapperScan({"com.policy.web.store.mapper"})
@EnableTransactionManagement
@Import({RestTemplate.class, PaginationInterceptor.class})
public class PolicyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PolicyApplication.class, args);
    }

}

