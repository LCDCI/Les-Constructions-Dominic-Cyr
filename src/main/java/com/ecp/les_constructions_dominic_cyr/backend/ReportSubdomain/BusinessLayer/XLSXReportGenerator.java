package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class XLSXReportGenerator {

    public byte[] generateXLSXReport(Map<String, Object> analyticsData,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate) throws Exception {

        Workbook workbook = new XSSFWorkbook();

        // Updated Sheet structure to match 5-page audit depth
        createSummarySheet(workbook, analyticsData, startDate, endDate);
        createProjectPerformanceSheet(workbook, analyticsData); // NEW
        createTrafficSourceSheet(workbook, analyticsData);      // NEW
        createGeographicSheet(workbook, analyticsData);        // UPDATED to City
        createDeviceSheet(workbook, analyticsData);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private void createSummarySheet(Workbook workbook, Map<String, Object> analyticsData,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        Sheet sheet = workbook.createSheet("Executive Summary");
        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("MASTER AUDIT: LES CONSTRUCTIONS DOMINIC CYR");
        titleRow.getCell(0).setCellStyle(createTitleStyle(workbook));

        rowNum++;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        sheet.createRow(rowNum++).createCell(0).setCellValue("Period: " + startDate.format(formatter) + " to " + endDate.format(formatter));
        sheet.createRow(rowNum++).createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(formatter));

        rowNum++;
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("Core Metric");
        headerRow.createCell(1).setCellValue("Value");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.getCell(1).setCellStyle(headerStyle);

        Map<String, Object> summary = (Map<String, Object>) analyticsData.get("summary");
        Map<String, Object> insights = (Map<String, Object>) analyticsData.get("businessInsights");

        createDataRow(sheet, rowNum++, "Total Users", summary.get("totalUsers"));
        createDataRow(sheet, rowNum++, "Total Sessions", summary.get("totalSessions"));
        createDataRow(sheet, rowNum++, "Total Page Views", summary.get("totalPageViews"));
        createDataRow(sheet, rowNum++, "Avg. Bounce Rate", String.format("%.2f%%", summary.get("avgBounceRate")));
        createDataRow(sheet, rowNum++, "Scroll Rate", String.format("%.2f%%", summary.get("scrollRate")));

        rowNum++;
        Row insightHeader = sheet.createRow(rowNum++);
        insightHeader.createCell(0).setCellValue("Strategic Insights");
        insightHeader.getCell(0).setCellStyle(headerStyle);

        createDataRow(sheet, rowNum++, "Reader Intent Score", insights.get("readerIntent"));
        createDataRow(sheet, rowNum++, "Recommendation", insights.get("recommendation"));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createProjectPerformanceSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Project Performance");
        String[] headers = {"Project Path (URL)", "Views"};
        renderMapToSheet(workbook, sheet, headers, (Map<String, Long>) analyticsData.get("pageViewsData"));
    }

    private void createTrafficSourceSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Traffic Acquisition");
        String[] headers = {"Source / Medium", "Users"};
        renderMapToSheet(workbook, sheet, headers, (Map<String, Integer>) analyticsData.get("sourceData"));
    }

    private void createGeographicSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("City Intelligence");
        String[] headers = {"City", "Active Users"};
        renderMapToSheet(workbook, sheet, headers, (Map<String, Integer>) analyticsData.get("cityData"));
    }

    private void createDeviceSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Device Segmentation");
        String[] headers = {"Device Category", "Users"};
        renderMapToSheet(workbook, sheet, headers, (Map<String, Integer>) analyticsData.get("deviceData"));
    }

    private void renderMapToSheet(Workbook workbook, Sheet sheet, String[] headers, Map<String, ? extends Number> data) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        if (data != null) {
            List<? extends Map.Entry<String, ? extends Number>> sortedEntries = data.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue().doubleValue(), e1.getValue().doubleValue()))
                    .toList();

            for (Map.Entry<String, ? extends Number> entry : sortedEntries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().doubleValue());
            }
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value.toString() : "N/A");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); // Matching construction brand
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }
}