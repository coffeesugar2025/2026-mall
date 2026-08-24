package com.example.aiops.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * RAG 检索服务单元测试（Mock EmbeddingStore）
 */
@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private EmbeddingModel embeddingModel;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        when(embeddingModel.embed(any(String.class)))
                .thenReturn(new dev.langchain4j.model.output.Response<>(Embedding.from(new float[]{0.1f, 0.2f})));

        ragService = new RagService(embeddingStore, embeddingModel);
    }

    @Test
    void searchShouldReturnEmptyWhenNoMatch() {
        when(embeddingStore.findRelevant(any(Embedding.class), anyInt()))
                .thenReturn(List.of());

        String result = ragService.search("任意查询");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchShouldConcatenateMatchedTexts() {
        TextSegment seg1 = TextSegment.from("文档片段一：DB 连接超时排查");
        TextSegment seg2 = TextSegment.from("文档片段二：连接池配置建议");

        EmbeddingMatch<TextSegment> m1 = new EmbeddingMatch<>(0.9, "id1", seg1, null);
        EmbeddingMatch<TextSegment> m2 = new EmbeddingMatch<>(0.8, "id2", seg2, null);

        when(embeddingStore.findRelevant(any(Embedding.class), anyInt()))
                .thenReturn(List.of(m1, m2));

        String result = ragService.search("order-service 500");
        assertTrue(result.contains("文档片段一"));
        assertTrue(result.contains("文档片段二"));
    }

    @Test
    void searchShouldRespectTopN() {
        // 验证 topN=3 被正确传入
        when(embeddingStore.findRelevant(any(Embedding.class), ArgumentMatchers.eq(3)))
                .thenReturn(List.of());

        ragService.search("测试");
        // 若能正常返回（不抛异常），说明 topN 参数链路通畅
        assertDoesNotThrow(() -> ragService.search("测试"));
    }
}
