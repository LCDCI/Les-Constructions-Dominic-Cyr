package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PDFReportGeneratorTest {

    private PDFReportGenerator generator;
    private Map<String, Object> mockData;

    @BeforeEach
    void setUp() {
        generator = new PDFReportGenerator();
        mockData = new HashMap<>();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", 100L);
        summary.put("totalSessions", 200L);
        summary.put("scrollRate", 45.5);
        mockData.put("summary", summary);

        List<Map<String, Object>> projectAnalysis = new ArrayList<>();
        projectAnalysis.add(Map.of("path", "/projects/1", "piiScore", 85.0, "volatility", 2.1, "engagementLevel", "HOT"));
        projectAnalysis.add(Map.of("path", "/projects/2", "piiScore", 30.0, "volatility", 0.5, "engagementLevel", "COLD"));
        mockData.put("projectAnalysis", projectAnalysis);

        mockData.put("cityData", Map.of("Montreal", 50, "Quebec", 30));
        mockData.put("sourceData", Map.of("google", 40, "direct", 20));

        Map<String, Map<String, Object>> daily = new HashMap<>();
        daily.put("2023-01-01", Map.of("activeUsers", 10L));
        mockData.put("dailyMetrics", daily);

        mockData.put("businessInsights", Map.of("readerIntent", "HIGH", "recommendation", "Test Rec"));
    }

    @Test
    void generatePDFReport_Success() throws Exception {
        byte[] result = generator.generatePDFReport(mockData, LocalDateTime.now().minusDays(7), LocalDateTime.now());
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generatePDFReport_EmptyData_ThrowsException() {
        assertThrows(Exception.class, () -> generator.generatePDFReport(new HashMap<>(), LocalDateTime.now(), LocalDateTime.now()));
    }
}