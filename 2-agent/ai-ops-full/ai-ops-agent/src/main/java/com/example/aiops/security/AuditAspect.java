package com.example.aiops.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 可观测性 / 审计切面：
 * 对所有 Agent Service 方法做调用审计（入参脱敏、耗时、结果摘要），
 * 满足企业合规对"AI 决策可追溯"的要求。
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AIOPS_AUDIT");

    @Around("execution(* com.example.aiops.service.AgentService.*(..))")
    public Object auditAgent(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().getName();
        try {
            Object result = pjp.proceed();
            audit.info("AUDIT method={} durationMs={} status=SUCCESS",
                    method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            audit.error("AUDIT method={} durationMs={} status=FAILED reason={}",
                    method, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}
