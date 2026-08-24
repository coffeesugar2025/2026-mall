package com.example.aiops.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 安全/合规工具集（垂直 Agent 用）：
 *  - 审计日志分析
 *  - 合规文档检查
 *  - 风险等级评估
 */
@Component
public class SecurityTools {

    @Tool("分析最近的安全审计日志，返回异常访问/权限事件")
    public List<String> analyzeAuditLog(@P("最近N小时") int hours) {
        return List.of(
                "[" + hours + "h] 异常：账号 ops-01 非工作时间多次 root 登录",
                "[" + hours + "h] 异常：外部 IP 批量访问 /admin API"
        );
    }

    @Tool("检查文本是否包含敏感/违规内容（内容审核）")
    public String moderateContent(@P("待检查文本") String text) {
        // 真实场景可接 OpenAI Moderation / 自建审核模型
        boolean suspicious = text != null && (text.contains("攻击") || text.contains("漏洞利用"));
        return suspicious ? "{\"flagged\":true,\"categories\":[\"policy\"],\"score\":0.87}"
                         : "{\"flagged\":false,\"score\":0.02}";
    }

    @Tool("对一组风险项评估整体风险等级，返回 HIGH/MEDIUM/LOW 及说明")
    public String assessRisk(@P("风险项数量") int riskCount,
                            @P("最高单项严重级 HIGH/MEDIUM/LOW") String highestSeverity) {
        String level = "HIGH".equalsIgnoreCase(highestSeverity) && riskCount > 2 ? "HIGH" : highestSeverity;
        return "{\"overallRisk\":\"" + level + "\",\"riskCount\":" + riskCount + ",\"action\":\"需安全团队复核\"}";
    }
}
