package com.example.aiops.agent;

import com.example.aiops.rag.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * OpsAgentTools 工具方法单元测试
 */
@ExtendWith(MockitoExtension.class)
class OpsAgentToolsTest {

    @Mock
    private RagService ragService;

    private OpsAgentTools tools;

    @BeforeEach
    void setUp() {
        tools = new OpsAgentTools(ragService);
    }

    @Test
    void searchWikiShouldDelegateToRagService() {
        when(ragService.search("DB 连接超时")).thenReturn("知识库内容片段");

        String result = tools.searchWiki("DB 连接超时");
        assertEquals("知识库内容片段", result);
    }

    @Test
    void searchWikiShouldReturnEmptyOnBlankKeyword() {
        when(ragService.search("")).thenReturn("");
        String result = tools.searchWiki("");
        assertNotNull(result);
    }

    @Test
    void queryLogsShouldReturnNonEmptyList() {
        List<String> logs = tools.queryLogs("order-service", 30);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
    }

    @Test
    void queryLogsShouldContainExpectedLevels() {
        List<String> logs = tools.queryLogs("order-service", 30);
        boolean hasError = logs.stream().anyMatch(l -> l.contains("ERROR"));
        boolean hasWarn = logs.stream().anyMatch(l -> l.contains("WARN"));
        assertTrue(hasError);
        assertTrue(hasWarn);
    }

    @Test
    void queryLogsShouldReflectServiceName() {
        List<String> logs = tools.queryLogs("payment-service", 10);
        assertTrue(logs.stream().allMatch(l -> l.contains("payment-service")));
    }

    @Test
    void createTicketShouldReturnTicketIdWithPrefix() {
        String ticket = tools.createTicket("order-service 频繁 500", "P1", "疑似 DB 连接池耗尽");
        assertNotNull(ticket);
        assertTrue(ticket.contains("TICKET-"));
    }

    @Test
    void createTicketShouldContainUuidSuffix() {
        String t1 = tools.createTicket("标题1", "P2", "描述1");
        String t2 = tools.createTicket("标题2", "P3", "描述2");
        // 两次生成的工单号应不同（UUID 随机）
        assertNotEquals(t1, t2);
    }
}
