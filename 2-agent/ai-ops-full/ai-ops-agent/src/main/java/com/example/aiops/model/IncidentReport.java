package com.example.aiops.model;

import java.util.List;

/**
 * 结构化输出：事件分析报告（LLM 响应自动映射为 POJO）
 */
public record IncidentReport(
        String incident,
        String rootCause,
        String impact,
        String suggestion,
        List<String> actions,
        String ticketId,
        int riskLevel  // 1-5
) {}
