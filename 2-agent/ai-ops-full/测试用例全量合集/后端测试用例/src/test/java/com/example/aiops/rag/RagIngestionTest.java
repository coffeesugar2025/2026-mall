package com.example.aiops.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RAG 端到端集成测试：文档摄取 → 嵌入写入 → 检索召回
 */
class RagIngestionTest {

    @Test
    void ingestMarkdownAndSearchShouldRecallRelevantSegment(@TempDir Path tmpDir) throws IOException {
        // 准备：写入测试文档
        Path docFile = tmpDir.resolve("ops-guide.md");
        Files.writeString(docFile, """
                数据库连接池耗尽排查
                当应用出现 HikariPool Connection is not available 时，
                应检查 maximumPoolSize 与慢 SQL，并考虑扩容连接池。
                """);

        // Mock EmbeddingModel：对每个文本生成固定维度嵌入（简化，保证可检索）
        EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
        when(embeddingModel.embed(anyString())).thenAnswer(inv -> {
            String text = inv.getArgument(0);
            // 用文本哈希生成伪嵌入，使语义相近文本嵌入相近
            float[] vec = new float[8];
            for (int i = 0; i < 8; i++) vec[i] = (float) (text.hashCode() % 100) / 100f;
            return new dev.langchain4j.model.output.Response<>(Embedding.from(vec));
        });
        when(embeddingModel.dimension()).thenReturn(8);

        EmbeddingStore<dev.langchain4j.data.segment.TextSegment> store = new InMemoryEmbeddingStore<>(8);

        // 摄取文档
        Document doc = FileSystemDocumentLoader.loadDocument(docFile);
        dev.langchain4j.data.segment.TextSegment segment = dev.langchain4j.data.segment.TextSegment.from(doc.text());
        Embedding emb = embeddingModel.embed(doc.text()).content();
        store.add(emb, segment);

        // 检索
        Embedding queryEmb = embeddingModel.embed("连接池耗尽怎么排查").content();
        List<dev.langchain4j.store.embedding.EmbeddingMatch<dev.langchain4j.data.segment.TextSegment>> matches =
                store.findRelevant(queryEmb, 1);

        assertFalse(matches.isEmpty());
        assertTrue(matches.get(0).embedded().text().contains("连接池"));
    }

    // 辅助：anyString 静态导入
    private static String anyString() { return org.mockito.ArgumentMatchers.anyString(); }
}
