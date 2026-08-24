package com.example.aiops.agent;

import com.example.aiops.model.InvestmentAdvice;
import com.example.aiops.tools.OpsTools;
import com.example.aiops.tools.SecurityTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.output.StructuredOutput;
import org.springframework.stereotype.Component;

/**
 * 投资决策 Agent（Plan-and-Execute 风格，显式分步）：
 * 通过 AiServices 构建具备工具能力的 ReAct Agent，并封装"先规划、再分步执行、最后结构化汇总"的流程。
 *   Plan  ：让 LLM 产出执行计划
 *   Execute：依次调用知识库检索 / 日志查询 / IRR 计算等工具
 *   Summarize：将执行结果映射为结构化 InvestmentAdvice POJO
 */
@Component
public class InvestmentAgent {

    private final ChatModel chatModel;
    private final OpsTools opsTools;
    private final SecurityTools securityTools;
    private final ChatMemory chatMemory;

    // 具备工具调用能力的执行用 Agent
    private final Executor executor;

    public InvestmentAgent(ChatModel chatModel, OpsTools opsTools, SecurityTools securityTools, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.opsTools = opsTools;
        this.securityTools = securityTools;
        this.chatMemory = chatMemory;
        this.executor = AiServices.builder(Executor.class)
                .chatModel(chatModel)
                .tools(opsTools, securityTools)
                .chatMemory(chatMemory)
                .build();
    }

    /** 对外入口：对指定项目执行 Plan→Execute→Summarize，返回结构化投资建议 */
    public InvestmentAdvice advise(String userId, String project, String goal) {
        // 1) Plan：让模型产出计划（作为一条用户消息驱动）
        String planPrompt = "请为以下目标制定分步执行计划（仅列出步骤）：" + goal;
        executor.chat(userId, planPrompt);

        // 2) Execute：让 Agent 自主调用工具执行目标
        String executionResult = executor.chat(userId, goal).text();

        // 3) Summarize：将执行结果结构化
        Summarizer summarizer = AiServices.builder(Summarizer.class)
                .chatModel(chatModel)
                .build();
        return summarizer.summarize(project, executionResult);
    }

    /** 工具化执行 Agent（ReAct） */
    public interface Executor {
        dev.langchain4j.model.output.Response<String> chat(@MemoryId String userId,
                                                          @UserMessage String message);
    }

    /** 将 Agent 文本产出映射为结构化投资建议 POJO */
    public interface Summarizer {
        @StructuredOutput(schema = """
                {
                  "type":"object",
                  "properties":{
                    "project":{"type":"string"},
                    "estimatedIRR":{"type":"number"},
                    "estimatedNPV":{"type":"number"},
                    "conclusion":{"type":"string"},
                    "rationale":{"type":"string"},
                    "risks":{"type":"array","items":{"type":"string"}}
                  },
                  "required":["project","estimatedIRR","estimatedNPV","conclusion","rationale","risks"]
                }
                """)
        InvestmentAdvice summarize(@UserMessage String analysisText);
    }
}
