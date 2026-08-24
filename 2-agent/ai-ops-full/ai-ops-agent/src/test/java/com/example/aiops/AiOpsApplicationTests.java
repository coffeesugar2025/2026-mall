package com.example.aiops;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiOpsApplicationTests {

    @Test
    void contextLoads() {
        // 仅校验 Spring 上下文能正常装配（Agent/Tool/Memory 等 Bean 无循环依赖）
    }
}
