package com.example.aiops.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import dev.langchain4j.store.memory.chat.redis.RedisChatMemoryStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆配置：
 * - in-memory：本地开发/单实例
 * - redis：多实例共享、跨会话持久化（依赖 spring-boot-starter-data-redis）
 * 使用 MessageWindowChatMemory，按消息条数滑动窗口，保证多轮上下文连贯。
 */
@Configuration
public class ChatMemoryConfig {

    @Value("${aiops.memory.store:in-memory}")
    private String store;

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        if ("redis".equalsIgnoreCase(store)) {
            // langchain4j-redis：通过 host/port 构建（已引入 jedis 传递依赖）
            return RedisChatMemoryStore.builder()
                    .host("localhost")
                    .port(6379)
                    .build();
        }
        return new InMemoryChatMemoryStore();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryStore chatMemoryStore) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
    }
}
