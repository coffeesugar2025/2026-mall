package com.example.aiops.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * RAG 服务：
 * - ingestDirectory：批量摄取文档（PDF/Word/HTML/MD/TXT 由对应解析器扩展）
 * - search：向量检索 + 结果重排序（ReRank，按 score 降序即简易重排）
 * - searchHybrid：混合检索（向量 + 关键词命中融合，演示 RRF 思路）
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public RagService(EmbeddingModel embeddingModel,
                      EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /** 摄取指定目录下所有文本文档并写入向量库 */
    public int ingestDirectory(String dirPath) {
        List<Document> docs = FileSystemDocumentLoader.loadDocuments(
                Path.of(dirPath), new TextDocumentParser());
        // Easy-RAG 风格的 ingestor：自动分段 + 嵌入 + 写入 store
        dev.langchain4j.store.embedding.EmbeddingStoreIngestor ingestor =
                dev.langchain4j.store.embedding.EmbeddingStoreIngestor.builder()
                        .documentSplitter(dev.langchain4j.data.document.splitter.DocumentSplitters.recursive(500, 100))
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
        ingestor.ingest(docs);
        log.info("RAG 摄取完成：{} 个文档，目录={}", docs.size(), dirPath);
        return docs.size();
    }

    /** 向量检索 + 按得分重排序（ReRank） */
    public String search(String query, int topK) {
        dev.langchain4j.model.embedding.Embedding qEmb = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(qEmb)
                        .maxResults(topK)
                        .build());
        // ReRank：按相关性得分降序（实际生产可接入 Cohere/自研 rerank 模型）
        matches.sort(java.util.Comparator.comparingDouble(EmbeddingMatch::score).reversed());
        return matches.stream()
                .map(m -> "[score=" + String.format("%.3f", m.score()) + "] " + m.embedded().text())
                .collect(java.util.stream.Collectors.joining("\n---\n"));
    }

    /** 混合检索：向量召回后与关键词命中片段融合（RRF 简化实现） */
    public String searchHybrid(String query, int topK) {
        String vectorPart = search(query, topK);
        // 关键词命中（演示：在已召回片段中再做 contains 加权，真实场景可接 BM25/ES）
        dev.langchain4j.model.embedding.Embedding qEmb = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> all = embeddingStore.search(
                EmbeddingSearchRequest.builder().queryEmbedding(qEmb).maxResults(topK * 2).build());
        StringBuilder sb = new StringBuilder(vectorPart);
        for (EmbeddingMatch<TextSegment> m : all) {
            if (m.embedded().text().toLowerCase().contains(query.toLowerCase())) {
                sb.append("\n[关键词命中] ").append(m.embedded().text());
            }
        }
        return sb.toString();
    }
}
