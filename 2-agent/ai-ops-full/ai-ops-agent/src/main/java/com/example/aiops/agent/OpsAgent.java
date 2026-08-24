package com.example.aiops.agent;

import com.example.aiops.model.IncidentReport;
import com.example.aiops.tools.OpsTools;
import com.example.aiops.tools.SecurityTools;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.output.StructuredOutput;

/**
 * 运维 ReAct Agent（声明式 AiService）：
 * - 绑定 ChatModel / Tools / ChatMemory
 * - chat：多轮流式对话，Agent 自主规划并调用工具
 * - analyzeIncident：结构化输出（JSON Schema 约束 → POJO）
 */
@AiService(
        wiringMode = AiService.WiringMode.EXPLICIT,
        chatModel = "chatModel",
        tools = {OpsTools.class, SecurityTools.class},
        chatMemory = "chatMemory"
)
public interface OpsAgent {

    /** 多轮对话（流式返回） */
    TokenStream chat(@MemoryId String userId,
                     @UserMessage String userMessage);

    /**
     * 结构化事件分析：LLM 输出经 JSON Schema 校验后映射为 IncidentReport POJO。
     */
    @StructuredOutput(schema = """
            {
              "type": "object",
              "properties": {
                "incident":   {"type": "string"},
                "rootCause":  {"type": "string"},
                "impact":     {"type": "string"},
                "suggestion": {"type": "string"},
                "actions":    {"type": "array", "items": {"type": "string"}},
                "ticketId":   {"type": "string"},
                "riskLevel":  {"type": "integer", "minimum": 1, "maximum": 5}
              },
              "required": ["incident","rootCause","impact","suggestion","actions","ticketId","riskLevel"]
            }
            """)
    IncidentReport analyzeIncident(@MemoryId String userId,
                                   @UserMessage String incidentDescription);
}
