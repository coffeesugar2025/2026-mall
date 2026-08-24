package com.example.aiops.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 向量存储配置：
 * - in-memory：开箱即跑，无需外部中间件（演示/开发）
 * - milvus：生产级分布式向量库（需在 application.yml 切换）
 */
@Configuration
public class RagConfig {

    @Value("${aiops.rag.store:in-memory}")
    private String store;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {
        if ("milvus".equalsIgnoreCase(store)) {
            // 生产环境启用 Milvus（依赖 langchain4j-milvus 已引入）
            return dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore.builder()
                    .host("localhost")
                    .port(19530)
                    .collectionName("aiops_kb")
                    .dimension(1536)
                    .build();
        }
        return new InMemoryEmbeddingStore<>();
    }
}
