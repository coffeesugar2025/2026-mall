# AI Ops Hub · 前端 (Vite + Vue3 + Element Plus)

企业级智能运维 Agent 平台前端，配套后端 `ai-ops-agent`（Spring Boot + LangChain4j 1.19）。

## 功能页面
- `/chat` 智能对话：ReAct / Supervisor 多 Agent 流式对话（SSE），工具调用过程可视化
- `/knowledge` 知识库：RAG 文档摄取 + 语义检索（向量 + ReRank）
- `/incident` 事件分析：结构化输出（POJO）+ 自动建工单
- `/invest` 投资决策：Plan-and-Execute Agent + IRR/NPV 计算 + ECharts 可视化
- `/monitor` 系统监控：KPI 卡片 + 调用趋势 + 审计日志表

## 启动
```bash
cd frontend
npm install      # 或 pnpm install / yarn
npm run dev      # 开发模式，默认 http://localhost:5173
npm run build    # 生产构建，产物在 dist/
```

## 接口代理
Vite 已配置 `/api` 代理到 `http://localhost:8080`（见 `vite.config.js`），后端需先启动并暴露：
- `POST /api/ai/chat?userId=` （SSE 流式）
- `POST /api/ai/analyze?userId=`
- `POST /api/ai/invest/advise?userId=&project=`
- `POST /api/ai/rag/ingest?dir=` 、`/ingest-text`、`GET /api/ai/rag/search?q=`

## 说明
- 用户 ID 可在顶部随时切换，用于演示 ChatMemory 多用户隔离。
- 监控/知识库部分为演示占位数据，可接真实 `/actuator` 或审计接口替换。
