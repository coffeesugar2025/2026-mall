package com.example.aiops.tools;

import com.example.aiops.rag.RagService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 运维/安全/流程 工具集：
 * 通过 @Tool 注解将 Java 方法暴露给 LLM，Agent 自主决定调用。
 * 覆盖：知识库检索、日志查询、依赖漏洞扫描、工单创建、健康检查、外部API调用。
 */
@Component
public class OpsTools {

    private static final Logger log = LoggerFactory.getLogger(OpsTools.class);

    private final RagService ragService;
    private final Map<String, String> ticketStore = new ConcurrentHashMap<>();

    public OpsTools(RagService ragService) {
        this.ragService = ragService;
    }

    @Tool("从企业运维知识库检索排查/运维文档（RAG 向量检索）")
    public String searchWiki(@P("检索关键词或问题描述") String keyword) {
        String result = ragService.search(keyword, 3);
        return result.isBlank() ? "知识库暂无相关文档。" : result;
    }

    @Tool("查询指定微服务最近 N 分钟的应用错误日志")
    public List<String> queryLogs(@P("服务名，如 order-service") String serviceName,
                                  @P("最近多少分钟") int minutes) {
        log.info("Tool 调用：queryLogs service={}, minutes={}", serviceName, minutes);
        // 模拟日志系统查询（真实场景接 ELK/Loki）
        return List.of(
                "[" + serviceName + "][" + minutes + "m内] ERROR DB connection timeout (pool exhausted)",
                "[" + serviceName + "] WARN High GC pause 1200ms",
                "[" + serviceName + "] ERROR /api/order 500 in 3.2% requests"
        );
    }

    @Tool("扫描指定模块的 Maven 依赖漏洞，返回 CVE 列表（结构化）")
    public List<VulnItem> scanDependencies(@P("项目模块名") String module) {
        log.info("Tool 调用：scanDependencies module={}", module);
        return List.of(
                new VulnItem("CVE-2025-12345", "commons-collections", "3.2.1", "HIGH",
                        "升级到 3.2.2+ 或迁移 commons-collections4"),
                new VulnItem("CVE-2025-67890", "log4j-core", "2.17.0", "MEDIUM",
                        "升级到 2.24.0+")
        );
    }

    @Tool("对一组现金流计算项目内部收益率 IRR（投资决策辅助）")
    public double calcIRR(@P("初始投资额（正数）") double initialInvestment,
                          @P("各期净现金流列表") List<Double> cashFlows) {
        // 简易 IRR：牛顿法
        java.util.List<Double> cf = new java.util.ArrayList<>();
        cf.add(-initialInvestment);
        cf.addAll(cashFlows);
        double r = 0.1;
        for (int iter = 0; iter < 200; iter++) {
            double npv = 0, dnpv = 0;
            for (int t = 0; t < cf.size(); t++) {
                npv += cf.get(t) / Math.pow(1 + r, t);
                if (t > 0) dnpv -= t * cf.get(t) / Math.pow(1 + r, t + 1);
            }
            if (Math.abs(npv) < 1e-6) break;
            double newR = r - npv / dnpv;
            if (Math.abs(newR - r) < 1e-8) break;
            r = newR;
        }
        return Math.round(r * 10000.0) / 10000.0;
    }

    @Tool("创建运维/安全工单并返回工单号")
    public String createTicket(@P("工单标题") String title,
                              @P("优先级 P1/P2/P3/P4") String priority,
                              @P("问题/处置描述") String description) {
        String id = "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        ticketStore.put(id, "[%s] %s | %s | %s".formatted(priority, title, description, OffsetDateTime.now()));
        log.info("Tool 调用：createTicket id={}", id);
        return "工单已创建：" + id;
    }

    @Tool("获取指定服务的当前健康状态（调用外部监控 API 示例）")
    public String healthCheck(@P("服务名") String serviceName) {
        return "{\"service\":\"" + serviceName + "\",\"status\":\"DEGRADED\",\"cpu\":78,\"memory\":82,\"errorRate\":0.032}";
    }

    /** 结构化漏洞项（供 Agent 结构化输出 / JSON Schema 约束） */
    public record VulnItem(String cveId, String artifact, String version,
                           String severity, String suggestion) {}
}
