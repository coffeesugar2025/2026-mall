
# HR请假业务全链路原型代码
> 业务场景：员工申请请假3天完整AI审批链路
> 组件栈：Vue3 + SpringBoot3 Java17 + LangChain4j 1.19.0 + OpenClaw + Ollama + Milvus + n8n + MySQL

## 整体业务流程图（带编号步骤）
```mermaid
flowchart TD
    subgraph 客户端
        U["员工：我想请假3天"]
        VUE["Vue3前端"]
    end

    subgraph 业务层
        SB["SpringBoot3<br/>鉴权、参数校验、MySQL落请假单"]
    end

    subgraph AI推理链路
        LC["LangChain4j"]
        MILVUS["Milvus向量库<br/>休假制度RAG知识库"]
        OLL["Ollama私有化大模型"]
    end

    subgraph Agent执行层
        OC["OpenClaw<br/>MCP‑HR‑Skills技能"]
        HRM["HRM/OA系统<br/>查假期余额、直属领导ID"]
    end

    subgraph 工作流层
        N8N["n8n请假审批工作流"]
    end

    DB[(MySQL业务数据库)]

    U -->|①提交请假：userId、3天、时间范围| VUE
    VUE -->|②HTTP JSON表单数据| SB
    SB -->|③写入请假申请，状态=AI校验中| DB

    SB -->|④业务上下文传给LangChain4j| LC
    LC <-->|⑤RAG读取企业休假制度片段| MILVUS
    LC -->|⑥组装完整Prompt，调用Ollama推理| OLL
    OLL -->|⑦返回意图：需要查询员工假期&审批人| LC

    LC -->|⑧工具调用指令回传给SpringBoot3| SB
    SB -->|⑨下发任务指令+用户上下文| OC
    OC -->|⑩MCP协议调用HR技能| HRM
    HRM -->|⑪返回真实业务：剩余假期、领导ID| OC
    OC -->|⑫携带业务数据再次调用Ollama二次判断| OLL
    OLL -->|⑬输出结论：条件满足，发起审批流程| OC

    OC -->|⑭任务结果回传SpringBoot3| SB
    SB -->|⑮Webhook触发n8n审批流程| N8N

    N8N -->|⑯领导审批通过| HRM
    N8N -->|⑯领导审批驳回| SB

    HRM -->|⑰写入正式请假台账| SB
    SB -->|⑱更新请假单最终状态| DB
    SB -->|⑲返回审批结果JSON| VUE
    VUE -->|⑳页面展示结果给员工| U
```
