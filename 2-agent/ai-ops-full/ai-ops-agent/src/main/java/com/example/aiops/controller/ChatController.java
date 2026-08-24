package com.example.aiops.controller;

import com.example.aiops.model.IncidentReport;
import com.example.aiops.model.InvestmentAdvice;
import com.example.aiops.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 统一入口 Controller：
 *  - POST /ai/chat           : 流式对话（SSE），Agent 自主规划+调工具+返回
 *  - POST /ai/analyze        : 结构化事件分析（返回 IncidentReport JSON）
 *  - POST /ai/invest/advise  : Plan-and-Execute 投资决策（返回 InvestmentAdvice JSON）
 *  - POST /ai/rag/ingest     : 触发 RAG 文档摄取
 */
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final AgentService agentService;
    private final com.example.aiops.rag.RagService ragService;

    public ChatController(AgentService agentService,
                          com.example.aiops.rag.RagService ragService) {
        this.agentService = agentService;
        this.ragService = ragService;
    }

    /** 流式对话（Server-Sent Events） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam(defaultValue = "anonymous") String userId,
                            @RequestBody String message) {
        return Flux.create(sink -> agentService.chat(userId, message)
                .onNext(token -> { sink.next(token); sink.complete(); })
                .onError(sink::error)
                .start());
    }

    /** 结构化事件分析 */
    @PostMapping("/analyze")
    public Mono<IncidentReport> analyze(@RequestParam(defaultValue = "anonymous") String userId,
                                        @RequestBody String description) {
        return Mono.fromCallable(() -> agentService.analyzeIncident(userId, description));
    }

    /** Plan-and-Execute 风格投资建议 */
    @PostMapping("/invest/advise")
    public Mono<InvestmentAdvice> advise(@RequestParam(defaultValue = "anonymous") String userId,
                                         @RequestParam String project,
                                         @RequestBody String goal) {
        return Mono.fromCallable(() -> agentService.investmentAdvise(userId, project, goal));
    }

    /** 触发 RAG 文档摄取（演示入口） */
    @PostMapping("/rag/ingest")
    public Mono<java.util.Map<String, Object>> ingest(@RequestParam String dir) {
        int n = ragService.ingestDirectory(dir);
        return Mono.just(java.util.Map.of("ingested", n, "directory", dir));
    }
}
