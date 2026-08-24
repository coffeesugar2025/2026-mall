# AI Ops Hub —— 企业智能运营中枢（LangChain4j 全功能）

一个**前后端一体、功能全面**的企业级智能运维 Agent 平台示例，基于 **Spring Boot 3.2 + LangChain4j 1.19（Java 17）** 与 **Vite + Vue3 + Element Plus**。

## 功能覆盖（LangChain4j 能力 → 实现）

| 能力 | 后端实现 | 前端页面 |
|---|---|---|
| 统一 LLM 接入 | `config/LangChain4jConfig`（OpenAI gpt-4o，可切 Azure/通义/Ollama） | — |
| RAG 全链路（摄取/分段/嵌入/检索/ReRank/混合检索） | `rag/RagConfig`、`rag/RagService`（InMemory 默认，Milvus 可切换） | 知识库页（摄取 + 检索） |
| ReAct Agent + `@Tool` 多工具 | `agent/OpsAgent`（知识库/日志/IRR/工单/健康检查） | 智能对话页 |
| Plan-and-Execute Agent | `agent/InvestmentAgent`（规划→分步调工具→结构化汇总） | 投资决策页 |
| Supervisor 多 Agent 协作 | `agent/SupervisorAgent`（意图路由到 ops/invest/security 子 Agent） | 智能对话页（统一入口） |
| 结构化输出（POJO + JSON Schema） | `IncidentReport` / `InvestmentAdvice` | 事件分析 / 投资决策页 |
| 对话记忆（多用户隔离） | `memory/ChatMemoryConfig`（InMemory/Redis 可切） | 顶部用户切换 |
| 流式输出（SSE） | `TokenStream` + Controller | 对话页流式打字机 |
| Spring Boot 集成 | Spring Boot Starter | — |
| 安全 & 审计 | `tools/SecurityTools`（审核/风险评估）+ `security/AuditAspect` | 系统监控页（审计日志） |
| 可观测性 | 审计切面记录耗时/状态 | 系统监控页（KPI/趋势图） |

## 目录结构
```
ai-ops-full/
├── ai-ops-agent/   # 后端 Spring Boot 工程（含 mvnw 包装器）
└── frontend/       # 前端 Vite + Vue3 工程
```

## 快速启动

### 1) 后端
```bash
export OPENAI_API_KEY=sk-xxx
cd ai-ops-agent
./mvnw spring-boot:run          # Mac/Linux
mvnw.cmd spring-boot:run        # Windows
# 默认监听 http://localhost:8080
```

### 2) 前端
```bash
cd frontend
npm install
npm run dev                     # 默认 http://localhost:5173
```
Vite 已配置 `/api` 代理到 `http://localhost:8080`，无需处理跨域。

### 3) 生产构建
```bash
cd frontend && npm run build    # 产物 dist/，可交由后端静态托管或 Nginx 部署
```

## 接口契约（前端已对接）
- `POST /api/ai/chat?userId=` —— SSE 流式对话（Supervisor 自动路由多 Agent）
- `POST /api/ai/analyze?userId=` —— 结构化事件分析（返回 IncidentReport JSON）
- `POST /api/ai/invest/advise?userId=&project=` —— 投资决策（返回 InvestmentAdvice）
- `POST /api/ai/rag/ingest?dir=` 、`/ingest-text` —— 文档摄取
- `GET  /api/ai/rag/search?q=` —— 语义检索

## 说明
- 默认 `in-memory` 模式**无需任何外部中间件**，开箱即跑；生产环境在 `application.yml` 将 `aiops.rag.store` 改为 `milvus`、`aiops.memory.store` 改为 `redis` 即可（对应依赖已引入，业务代码零改动）。
- 沙盒环境仅含 JDK 11 且无 Maven，未在此执行编译验证；API 用法已按 LangChain4j 1.19 官方文档核对。若本地编译遇个别 API 微调点，提供报错即可定位修改。
