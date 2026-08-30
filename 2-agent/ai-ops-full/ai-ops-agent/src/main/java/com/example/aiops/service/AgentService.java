package com.example.aiops.service;

import com.example.aiops.agent.InvestmentAgent;
import com.example.aiops.agent.OpsAgent;
import com.example.aiops.agent.SupervisorAgent;
import com.example.aiops.model.IncidentReport;
import com.example.aiops.model.InvestmentAdvice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * Agent 编排 Service：
 * 对 Controller 暴露统一方法，内部按 aiops.agent.mode 路由到
 *   - ReAct 单 Agent（OpsAgent）
 *   - Plan-and-Execute（InvestmentAgent）
 *   - Supervisor 多 Agent 协作（SupervisorAgent）
 */
@Service
public class AgentService {

    private final OpsAgent opsAgent;
    private final InvestmentAgent investmentAgent;
    private final SupervisorAgent supervisorAgent;
    private final String mode;

    public AgentService(OpsAgent opsAgent,
                        InvestmentAgent investmentAgent,
                        SupervisorAgent supervisorAgent,
                        Environment env) {
        this.opsAgent = opsAgent;
        this.investmentAgent = investmentAgent;
        this.supervisorAgent = supervisorAgent;
        this.mode = env.getProperty("aiops.agent.mode", "supervisor");
    }

    /** 流式对话 */
    public TokenStream chat(String userId, String message) {
        if ("supervisor".equalsIgnoreCase(mode)) {
            String result = supervisorAgent.handle(userId, message);
            return new SimpleTokenStream(result);
        }
        return opsAgent.chat(userId, message);
    }

    /** 结构化事件分析 */
    public IncidentReport analyzeIncident(String userId, String description) {
        return opsAgent.analyzeIncident(userId, description);
    }

    /** Plan-and-Execute 风格投资决策 */
    public InvestmentAdvice investmentAdvise(String userId, String project, String goal) {
        return investmentAgent.advise(userId, project, goal);
    }

    /** 将完整文本封装为一次性 TokenStream（供 Supervisor 模式复用同一接口） */
    static final class SimpleTokenStream implements TokenStream {
        private final String text;
        SimpleTokenStream(String text) { this.text = text; }
//        @Override
//        public TokenStream onNext(java.util.function.Consumer<String> c) { c.accept(text); return this; }

        @Override
        public TokenStream onPartialResponse(Consumer<String> consumer) {
            return null;
        }

        @Override
        public TokenStream onRetrieved(Consumer<List<Content>> consumer) {
            return null;
        }

        @Override
        public TokenStream onToolExecuted(Consumer<ToolExecution> consumer) {
            return null;
        }

        @Override
        public TokenStream onCompleteResponse(Consumer<ChatResponse> consumer) {
            return null;
        }

        @Override
        public TokenStream onError(java.util.function.Consumer<Throwable> c) { return this; }

        @Override
        public TokenStream ignoreErrors() {
            return null;
        }

        @Override
        public void start() {

        }

//        @Override
//        public TokenStream onComplete(java.util.function.Consumer<String> c) { c.accept(text); return this; }
//        @Override
//        public String text() { return text; }
//        @Override
//        public TokenStream start() { return this; }
//        @Override
//        public TokenStream abort() { return this; }
    }
}
