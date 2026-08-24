package com.example.aiops.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对话记忆配置测试
 */
@SpringBootTest
@TestPropertySource(properties = {
        "aiops.memory.store=in-memory"
})
class ChatMemoryConfigTest {

    @Autowired(required = false)
    private ChatMemoryConfig chatMemoryConfig;

    @Test
    void contextLoads() {
        // 配置类应能被 Spring 容器加载
        assertNotNull(chatMemoryConfig);
    }

    @Test
    void chatMemoryBeanShouldBeMessageWindowType() {
        ChatMemory memory = chatMemoryConfig.chatMemory();
        assertNotNull(memory);
        assertInstanceOf(MessageWindowChatMemory.class, memory);
    }

    @Test
    void chatMemoryShouldAcceptMessages() {
        ChatMemory memory = chatMemoryConfig.chatMemory();
        // MessageWindowChatMemory 默认 maxMessages=20
        assertEquals(20, ((MessageWindowChatMemory) memory).maxMessages());
    }
}
