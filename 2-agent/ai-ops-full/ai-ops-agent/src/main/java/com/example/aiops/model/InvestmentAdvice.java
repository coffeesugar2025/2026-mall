package com.example.aiops.model;

/**
 * 结构化输出：投资建议报告（Plan-and-Execute Agent 最终汇总）
 */
public record InvestmentAdvice(
        String project,
        double estimatedIRR,
        double estimatedNPV,
        String conclusion,   // 建议/谨慎/不建议
        String rationale,
        List<String> risks
) {}
