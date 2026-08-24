# ai-ops-agent —— 企业级 LangChain4j 智能运维/安全/投资分析 Agent

基于 **LangChain4j 1.19.0 + Spring Boot 3.2** 的企业级多能力智能体项目，
覆盖 LangChain4j 全部核心功能模块，开箱即跑（InMemory 模式无需外部中间件）。

## 功能覆盖（对应 LangChain4j 产品能力）

| 功能模块 | 本项目实现 |
|---------|-----------|
| 统一 LLM 接入 | `config/LangChain4jConfig`：OpenAI gpt-4o，统一 ChatModel/EmbeddingModel，可一键切换 Azure/通义/Ollama |
| 向量 & 嵌入 (RAG) | `rag/RagConfig` + `rag/RagService`：InMemory（默认）/ Milvus（生产），自动分段、嵌入、写入 |
| RAG 全链路 | 文档摄取 + 向量检索 + **ReRank 重排序** + **混合检索（向量+关键词/RRF 思路）** |
| 智能体 & 工具 | `tools/OpsTools`（知识库/日志/IRR/工单/健康检查）+ `tools/SecurityTools`（审计/审核/风险评估），`@Tool` 注解暴露 |
| Agent 类型 | **ReAct**（`OpsAgent`）、**Plan-and-Execute**（`InvestmentAgent`）、**Supervisor 多 Agent 协作**（`SupervisorAgent`） |
| 多工具并行 | AiServices 声明式 `tools = {OpsTools.class, SecurityTools.class}` |
| 对话记忆 | `memory/ChatMemoryConfig`：MessageWindowChatMemory，InMemory / Redis 可切换，多用户隔离 |
| 结构化输出 | `IncidentReport` / `InvestmentAdvice` POJO + JSON Schema 约束 + 自动校验映射 |
| AI Services | 全部 Agent 使用 `@AiService` / `AiServices.builder` 声明式动态代理 |
| 流式输出 | `TokenStream` + Controller SSE（`text/event-stream`） |
| 框架集成 | Spring Boot Starter + LangChain4j Spring Boot Starter |
| 安全 & 合规 | `SecurityTools` 内容审核 + `security/AuditAspect` 全链路审计 + `GlobalExceptionHandler` |
| 可观测性 | 审计切面记录每次 Agent 调用耗时/状态 |

## 快速开始

```bash
export OPENAI_API_KEY=sk-xxxx          # 设置 API Key
cd ai-ops-agent
./mvnw spring-boot:run                 # Mac/Linux
mvnw.cmd spring-boot:run               # Windows
```

> 未配置 Key 时以 `demo` 占位，启动可装配但不会真正调用模型。

## 接口示例

```bash
# 1) 流式对话（Supervisor 自动路由到 ops/invest/security 子 Agent）
curl -N -X POST "http://localhost:8080/ai/chat?userId=ops-001" \
     -H "Content-Type: text/plain" \
     -d "order-service 最近频繁 500，帮我分析原因并建工单"

# 2) 结构化事件分析（返回 IncidentReport JSON）
curl -X POST "http://localhost:8080/ai/analyze?userId=ops-001" \
     -H "Content-Type: text/plain" \
     -d "order-service 连续 30 分钟 500 错误，疑似 DB 连接池耗尽"

# 3) Plan-and-Execute 投资决策
curl -X POST "http://localhost:8080/ai/invest/advise?userId=biz-01&project=新产品线" \
     -H "Content-Type: text/plain" \
     -d "评估投入 500 万做新产品线、预计 3 年回本的可行性，给出 IRR 和建议"

# 4) 触发 RAG 文档摄取
curl -X POST "http://localhost:8080/ai/rag/ingest?dir=/path/to/docs"
```

## 切换到生产级组件

在 `application.yml` 修改：

```yaml
aiops:
  rag:
    store: milvus     # 改用 Milvus 分布式向量库（已引入 langchain4j-milvus）
  memory:
    store: redis      # 改用 Redis 持久化记忆（已引入 spring-boot-starter-data-redis）
```

并确保对应服务（Milvus / Redis）可达即可，业务代码无需改动。

## 项目结构

```
src/main/java/com/example/aiops/
├── AiOpsApplication.java        # 启动类
├── config/LangChain4jConfig.java # LLM/Embedding 配置
├── rag/                         # RAG：配置 + 检索/重排/混合检索服务
├── memory/ChatMemoryConfig.java # 对话记忆（InMemory/Redis）
├── tools/                       # @Tool 工具集：OpsTools + SecurityTools
├── agent/                       # OpsAgent(ReAct) / InvestmentAgent(Plan-Execute) / SupervisorAgent(多Agent)
├── model/                       # 结构化输出 POJO
├── service/AgentService.java    # Agent 编排（按 mode 路由）
├── controller/ChatController.java # REST + SSE 接口
└── security/                    # 审计切面 + 全局异常处理
```

## 版本

- LangChain4j 1.19.0
- Spring Boot 3.2.5
- Java 17+
