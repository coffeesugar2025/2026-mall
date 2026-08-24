package com.example.aiops.agent;

import com.example.aiops.tools.OpsTools;
import com.example.aiops.tools.SecurityTools;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import org.springframework.stereotype.Component;

/**
 * Supervisor 多 Agent 协作（监督者模式）：
 * 由 Supervisor Agent 识别用户意图，将请求路由到不同"子 Agent"能力域：
 *   - ops     : 运维排查（日志/知识库/工单）
 *   - invest  : 投资决策（IRR/NPV/建议）
 *   - security: 安全合规（漏洞/风险/审计）
 * 本类实现 Supervisor 路由层 + 三个子 Agent 接口（均通过 AiServices 声明式构建）。
 */
@Component
public class SupervisorAgent {

    private final ChatModel chatModel;
    private final OpsTools opsTools;
    private final SecurityTools securityTools;
    private final ChatMemory chatMemory;

    private final OpsSubAgent opsAgent;
    private final InvestSubAgent investAgent;
    private final SecuritySubAgent securityAgent;
    private final Router router;

    public SupervisorAgent(ChatModel chatModel, OpsTools opsTools, SecurityTools securityTools, ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.opsTools = opsTools;
        this.securityTools = securityTools;
        this.chatMemory = chatMemory;

        this.opsAgent = AiServices.builder(OpsSubAgent.class)
                .chatModel(chatModel).tools(opsTools, securityTools).chatMemory(chatMemory).build();
        this.investAgent = AiServices.builder(InvestSubAgent.class)
                .chatModel(chatModel).tools(opsTools, securityTools).chatMemory(chatMemory).build();
        this.securityAgent = AiServices.builder(SecuritySubAgent.class)
                .chatModel(chatModel).tools(opsTools, securityTools).chatMemory(chatMemory).build();
        this.router = AiServices.builder(Router.class).chatModel(chatModel).build();
    }

    /** 对外入口：Supervisor 先路由，再分发给对应子 Agent */
    public String handle(String userId, String message) {
        String target = router.route(message);
        return switch (target.toLowerCase()) {
            case "invest"  -> investAgent.run(userId, message);
            case "security" -> securityAgent.run(userId, message);
            default         -> opsAgent.run(userId, message); // 默认运维
        };
    }

    /** 路由 Agent：仅输出目标域名（ops/invest/security） */
    public interface Router {
        @SystemMessage("你是路由助手。根据用户消息判断意图，仅回复一个词：ops、invest 或 security。")
        String route(@UserMessage String message);
    }

    @SystemMessage("你是企业运维助手，可调用工具查询日志、检索知识库、创建工单。")
    public interface OpsSubAgent {
        String run(@MemoryId String userId, @UserMessage String message);
    }

    @SystemMessage("你是投资分析助手，可调用 IRR 计算等工具评估项目可行性并给出建议。")
    public interface InvestSubAgent {
        String run(@MemoryId String userId, @UserMessage String message);
    }

    @SystemMessage("你是安全合规助手，可调用漏洞扫描工具评估风险并给出整改建议。")
    public interface SecuritySubAgent {
        String run(@MemoryId String userId, @UserMessage String message);
    }
}
