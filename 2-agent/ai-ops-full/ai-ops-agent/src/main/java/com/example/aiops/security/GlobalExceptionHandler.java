package com.example.aiops.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理：统一返回结构，避免内部堆栈泄漏给客户端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handle(Exception e) {
        return ResponseEntity.status(500).body(
                Map.of("error", "AGENT_EXECUTION_FAILED", "message", e.getMessage()));
    }
}
