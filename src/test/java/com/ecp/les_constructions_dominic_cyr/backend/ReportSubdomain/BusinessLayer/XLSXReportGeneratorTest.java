package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XLSXReportGeneratorTest {

    private XLSXReportGenerator generator;
    private Map<String, Object> mockData;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        generator = new XLSXReportGenerator();
        startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2023, 1, 31, 23, 59);
        mockData = new HashMap<>();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", 150L);
        summary.put("totalSessions", 300L);
        summary.put("totalPageViews", 1200L);
        summary.put("avgBounceRate", 45.2);
        summary.put("scrollRate", 60.5);
        mockData.put("summary", summary);

        Map<String, Object> insights = new HashMap<>();
        insights.put("readerIntent", "HIGH");
        insights.put("recommendation", "Expand marketing in Montreal area.");
        mockData.put("businessInsights", insights);

        mockData.put("pageViewsData", Map.of("/projects/custom-home", 500L, "/contact", 100L));
        mockData.put("sourceData", Map.of("google", 80, "direct", 40));
        mockData.put("cityData", Map.of("Montreal", 90, "Laval", 30));
        mockData.put("deviceData", Map.of("mobile", 100, "desktop", 50));
    }

    @Test
    void generateXLSXReport_Success() throws Exception {
        byte[] result = generator.generateXLSXReport(mockData, startDate, endDate);

        assertNotNull(result);
        assertTrue(result.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertEquals(5, workbook.getNumberOfSheets());

            assertNotNull(workbook.getSheet("Executive Summary"));
            assertNotNull(workbook.getSheet("Project Performance"));
            assertNotNull(workbook.getSheet("Traffic Acquisition"));
            assertNotNull(workbook.getSheet("City Intelligence"));
            assertNotNull(workbook.getSheet("Device Segmentation"));

            Sheet summarySheet = workbook.getSheet("Executive Summary");
            assertEquals("MASTER AUDIT: LES CONSTRUCTIONS DOMINIC CYR",
                    summarySheet.getRow(0).getCell(0).getStringCellValue());

            Sheet citySheet = workbook.getSheet("City Intelligence");
            assertEquals("Montreal", citySheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals(90.0, citySheet.getRow(1).getCell(1).getNumericCellValue());
        }
    }

    @Test
    void generateXLSXReport_MissingOptionalData_HandlesGracefully() throws Exception {
        Map<String, Object> minimalData = new HashMap<>();
        minimalData.put("summary", mockData.get("summary"));
        minimalData.put("businessInsights", mockData.get("businessInsights"));
        minimalData.put("pageViewsData", null);
        minimalData.put("sourceData", null);

        byte[] result = generator.generateXLSXReport(minimalData, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet projectSheet = workbook.getSheet("Project Performance");
            assertNotNull(projectSheet);
            assertEquals(1, projectSheet.getPhysicalNumberOfRows()); // Only Header row exists
        }
    }

    @Test
    void generateXLSXReport_NullValuesInSummary_PrintsNA() throws Exception {
        Map<String, Object> summary = new HashMap<>(); // Empty summary
        mockData.put("summary", summary);

        byte[] result = generator.generateXLSXReport(mockData, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheet("Executive Summary");
            boolean foundNA = false;
            for (int i = 0; i < 15; i++) {
                if (sheet.getRow(i) != null && sheet.getRow(i).getCell(1) != null) {
                    if ("N/A".equals(sheet.getRow(i).getCell(1).getStringCellValue())) {
                        foundNA = true;
                        break;
                    }
                }
            }
            assertTrue(foundNA);
        }
    }

    @Test
    void generateXLSXReport_EmptyMapEntries_GeneratesEmptySheets() throws Exception {
        mockData.put("cityData", new HashMap<String, Integer>());

        byte[] result = generator.generateXLSXReport(mockData, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet citySheet = workbook.getSheet("City Intelligence");
            assertEquals(1, citySheet.getPhysicalNumberOfRows()); // Header only
        }
    }
}