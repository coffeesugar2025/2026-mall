package com.example.aiops.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Report（结构化输出 POJO）单元测试
 */
class ReportTest {

    @Test
    void recordShouldHoldAllFields() {
        Report r = new Report("根因", "影响", "建议", "TICKET-1");
        assertEquals("根因", r.rootCause());
        assertEquals("影响", r.impact());
        assertEquals("建议", r.suggestion());
        assertEquals("TICKET-1", r.ticketId());
    }

    @Test
    void equalsShouldBeValueBased() {
        Report r1 = new Report("a", "b", "c", "T1");
        Report r2 = new Report("a", "b", "c", "T1");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void toStringShouldContainFields() {
        Report r = new Report("rc", "im", "sg", "TK");
        String s = r.toString();
        assertTrue(s.contains("rc"));
        assertTrue(s.contains("im"));
        assertTrue(s.contains("sg"));
        assertTrue(s.contains("TK"));
    }
}
