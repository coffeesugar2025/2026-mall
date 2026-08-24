package com.example.aiops.controller;

import com.example.aiops.agent.OpsAgent;
import com.example.aiops.agent.Report;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.SpringBootDependencyInjectionTestExecutionListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ChatController API 集成测试（使用 WebTestClient + Mock Agent）
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(listeners = SpringBootDependencyInjectionTestExecutionListener.class)
class ChatControllerTest {

    @MockBean
    private OpsAgent opsAgent;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void analyzeShouldReturnStructuredReport() {
        Report mockReport = new Report("DB 连接池耗尽", "订单服务不可用", "扩容连接池并加监控", "TICKET-abc");
        when(opsAgent.analyzeIncident(anyString())).thenReturn(mockReport);

        webTestClient.post()
                .uri("/ai/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("order-service 连续 500 错误")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rootCause").isEqualTo("DB 连接池耗尽")
                .jsonPath("$.impact").isEqualTo("订单服务不可用")
                .jsonPath("$.suggestion").isEqualTo("扩容连接池并加监控")
                .jsonPath("$.ticketId").isEqualTo("TICKET-abc");
    }

    @Test
    void analyzeShouldReturnBadRequestOnBlankBody() {
        webTestClient.post()
                .uri("/ai/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("   ")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void chatShouldAcceptUserIdParam() {
        // chat 接口为流式 SSE，这里验证端点可访问且参数校验生效
        webTestClient.post()
                .uri("/ai/chat")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("帮我查一下 order-service 的日志")
                .exchange()
                .expectStatus().isBadRequest(); // 缺少 userId 参数应 400
    }
}
