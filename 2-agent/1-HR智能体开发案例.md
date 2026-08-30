## 一、总体架构图

展示从用户到底层算力/数据的完整链路与分层职责。

```mermaid 
graph TB
    subgraph 用户层
        U[HRBP / 招聘官 / 员工 / 管理者]
    end

    subgraph 前端层
        V[Vue 3 工作台<br/>Copilot对话/招聘台/员工服务/知识库/大屏]
    end

    subgraph Spring Boot 3 控制面与业务层
        SB1[SSE 代理 & 用户上下文注入]
        SB2[LangChain4j RAG 内核<br/>Embedding / PgVector / Augmentor]
        SB3[HR 业务服务<br/>员工 / 薪酬 / 招聘 / 请假 / 合规]
        SB4[MCP Server 工具层<br/>/mcp/tool/*]
        SB5[PII 脱敏 / RBAC / 审计]
    end

    subgraph OpenClaw Agent 层
        OC[OpenClaw Gateway :18789]
        A1[hr-recruit Agent]
        A2[hr-hrssc Agent]
        A3[hr-payroll Agent]
        A4[hr-compliance Agent]
        SK[Skills: hr-rag / hr-mcp / n8n-hook]
        MC[MCP Client]
    end

    subgraph 模型与算力层
        OL[Ollama 本地大模型<br/>qwen2.5:14b-instruct Q4]
        EX[Exo / vLLM AI 集群（可选高算力）]
    end

    subgraph 数据与自动化
        PG[(PostgreSQL 16 + pgvector<br/>业务表 + kb_chunk 向量表)]
        N8N[n8n 工作流引擎 :5678]
        HR[ATS / HRIS / 薪酬 / OA 系统]
        RD[(Redis 限流 / 会话)]
    end

    U --> V
    V --> SB1
    SB1 -->|代理 /v1/chat/completions| OC
    OC --> A1 & A2 & A3 & A4
    A1 & A2 & A3 & A4 --> SK
    SK --> MC
    MC -->|MCP over HTTP| SB4
    SK -->|RAG 检索| SB2
    OC -->|模型推理| OL
    OL --> OC
    SB4 --> SB3
    SB3 --> PG
    SB2 --> PG
    SB3 -->|Webhook 触发| N8N
    N8N --> HR
    SB3 --> HR
    SB5 -.横切.-> SB1 & SB2 & SB3 & SB4
    SB1 --> RD
    EX -.-> OL
```

## 二、技术架构图

聚焦技术组件、协议、端口与集成关系。

```mermaid 
graph LR
    subgraph FE["前端"]
        VUE["Vue 3<br/>Vite + TypeScript + Pinia<br/>Axios + EventSource SSE"]
    end

    subgraph SB["Spring Boot 3.2"]
        WEB["Spring MVC + Spring Security JWT"]
        SSEP["SseEmitter 流式接口<br/>api hr agent stream"]
        RAGC["LangChain4j RAG<br/>OllamaEmbeddingModel<br/>PgVectorEmbeddingStore<br/>Apache Tika 解析"]
        LLMC["LangChain4j OllamaChatModel<br/>仅用于工具内生成 非 Agent"]
        MCPS["MCP Java SDK 适配层<br/>mcp tool 端点"]
        JPA["Spring Data JPA + Hibernate"]
    end

    subgraph OC["OpenClaw Node.js 进程"]
        OCG["Gateway 端口18789"]
        OCM["MCP Client HTTP"]
        OCS["Skill Engine"]
    end

    subgraph INF["基础设施"]
        OLL["Ollama 端口11434<br/>qwen2.5 14b instruct"]
        EMB["Ollama Embedding<br/>nomic-embed-text"]
        PGV[("PostgreSQL + pgvector<br/>HNSW 向量索引")]
        N8["n8n 端口5678 Webhook"]
        RED[("Redis")]
        DK["Docker Compose / K8s"]
    end

    VUE -->|"HTTPS 和 REST"| WEB
    VUE -->|"SSE"| SSEP
    SSEP -->|"HTTP chat completions"| OCG
    OCG --> OCS
    OCS --> OCM
    OCM -->|"MCP JSON-RPC over HTTP"| MCPS
    OCS -->|"Skill 调 RAG"| RAGC
    RAGC --> EMB
    RAGC --> PGV
    RAGC --> JPA
    MCPS --> JPA
    JPA --> PGV
    LLMC -->|"生成请求"| OLL
    OCG -->|"模型推理"| OLL
    MCPS -->|"Webhook"| N8
    WEB --> RED
    EMB --> OLL
```

## 三、应用架构图

聚焦 HR 业务功能、Agent 划分、Skill 能力与后端服务的映射。

```mermaid 
graph TD
    subgraph V["Vue3 前端应用"]
        V1["HR Copilot 对话工作台"]
        V2["招聘协作台<br/>职位 候选人 匹配 面试"]
        V3["员工自助服务门户<br/>假期 社保 证明"]
        V4["知识库管理<br/>上传 切片 向量监控"]
        V5["流程看板 和 人才大屏"]
    end

    subgraph AG["OpenClaw 应用 Agents"]
        AG1["hr-recruit<br/>JD生成 简历解析 人岗匹配 面试安排"]
        AG2["hr-hrssc<br/>假期 报销 社保 证明开具"]
        AG3["hr-payroll<br/>工资单 个税 五险一金"]
        AG4["hr-compliance<br/>劳动法 合同 合规风险"]
        AG5["hr-performance<br/>OKR 绩效 面谈"]
    end

    subgraph SK["Skills 能力 OpenClaw"]
        S1["hr-rag Skill<br/>制度 劳动法 政策检索"]
        S2["hr-mcp Skill<br/>业务动作 请假 查薪 匹配"]
        S3["n8n-hook Skill<br/>触发审批 面试 入职流"]
    end

    subgraph BS["Spring Boot 后端服务"]
        B1["RAG 服务<br/>ingest / retrieve"]
        B2["员工服务<br/>Employee / Leave"]
        B3["薪酬服务<br/>Payroll / Tax"]
        B4["招聘服务<br/>Job / Candidate / Match"]
        B5["合规 和 绩效服务"]
        B6["集成服务<br/>n8n / ATS / HRIS"]
    end

    subgraph DT["数据与外部系统"]
        D1[("PostgreSQL + PgVector")]
        D2["ATS / HRIS / 薪酬系统"]
        D3["n8n 流程引擎"]
    end

    V1 --> AG1
    V1 --> AG2
    V1 --> AG3
    V1 --> AG4
    V1 --> AG5
    V2 --> AG1
    V3 --> AG2
    V4 --> B1
    V5 --> D3

    AG1 --> S1
    AG1 --> S2
    AG1 --> S3
    AG2 --> S1
    AG2 --> S2
    AG2 --> S3
    AG3 --> S1
    AG3 --> S2
    AG4 --> S1
    AG4 --> S2
    AG5 --> S1
    AG5 --> S2

    S1 --> B1
    S2 --> B2
    S2 --> B3
    S2 --> B4
    S2 --> B5
    S3 --> B6

    B1 --> D1
    B2 --> D1
    B3 --> D1
    B4 --> D1
    B5 --> D1
    B6 --> D2
    B6 --> D3
```

## 四、核心代码

4.1 OpenClaw 侧：配置与 Skill（Agent 大脑）

openclaw/openclaw.json（Agent、MCP Server、Skill 注册）：
```
{
  "model": { "default": "ollama/qwen2.5:14b-instruct" },
  "agents": {
    "hr-recruit": {
      "model": "ollama/qwen2.5:14b-instruct",
      "systemPromptFile": "SOUL.hr-recruit.md",
      "temperature": 0.6,
      "memory": { "backend": "sqlite", "path": "/root/.openclaw/memory/hr-recruit.sqlite" },
      "skills": ["hr-rag", "hr-mcp", "n8n-hook"],
      "channels": ["rest"]
    },
    "hr-hrssc": {
      "model": "ollama/qwen2.5:14b-instruct",
      "systemPromptFile": "SOUL.hr-hrssc.md",
      "temperature": 0.3,
      "skills": ["hr-rag", "hr-mcp", "n8n-hook"],
      "channels": ["rest"]
    },
    "hr-payroll": {
      "model": "ollama/qwen2.5:14b-instruct",
      "systemPromptFile": "SOUL.hr-payroll.md",
      "temperature": 0.1,
      "skills": ["hr-rag", "hr-mcp"],
      "channels": ["rest"]
    }
  },
  "mcp": {
    "servers": {
      "hr-backend": { "transport": "http", "url": "http://spring-hr:8080/mcp" },
      "rag-service": { "transport": "http", "url": "http://spring-hr:8080/mcp/rag" }
    }
  },
  "skills": {
    "entries": {
      "hr-rag": { "enabled": true, "path": "skills/hr-rag" },
      "hr-mcp": { "enabled": true, "path": "skills/hr-mcp" },
      "n8n-hook": { "enabled": true, "path": "skills/n8n-hook" }
    }
  }
}
```

openclaw/skills/hr-rag/SKILL.md（RAG 检索 Skill，声明必须先用 MCP 工具 rag_retrieve）

```
---
name: hr-rag
description: 检索 HR 制度、劳动法、个税与薪酬政策、招聘规范等知识库
trigger: "制度|劳动法|个税|年假|社保|公积金|政策|JD|简历"
tools:
  - name: rag_retrieve
    description: 从企业向量库检索相关知识片段（由 rag-service MCP Server 提供）
---
# HR RAG 检索 Skill
回答任何涉及 HR 制度、法律法规或历史知识的问题前，**必须先调用 `rag_retrieve`** 获取上下文，再基于上下文回答，并在结尾标注来源 doc_id。

调用约束（categories 按 agent 自动选择）：
- hr-hrssc -> ["HR_POLICY", "LABOR_LAW"]
- hr-payroll -> ["PAYROLL", "LABOR_LAW"]
- hr-recruit -> ["RECRUIT", "HR_POLICY"]
```
## 五、一句话串联执行流

员工问“年假怎么请” → Vue​ 调 /api/hr/agent/stream?agent=hr-hrssc → Spring​ 注入工号上下文并代理到 OpenClaw​ → OpenClaw hr-hrssc Agent 命中 hr-rag Skill → 通过 MCP 调 Spring rag_retrieve 拿制度片段 → 流式回答；员工说“帮我请 3 天” → Agent 调 hr-mcp Skill → Spring applyLeave 工具建单并 Webhook n8n​ → n8n 推送主管审批并回写状态 → 结果经 SSE 回到前端。
