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

        createSummarySheet(workbook, analyticsData, startDate, endDate);
        createDailyMetricsSheet(workbook, analyticsData);
        createGeographicSheet(workbook, analyticsData);
        createDeviceSheet(workbook, analyticsData);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        return baos.toByteArray();
    }

    private void createSummarySheet(Workbook workbook, Map<String, Object> analyticsData,
                                    LocalDateTime startDate, LocalDateTime endDate) {
        Sheet sheet = workbook.createSheet("Summary");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Analytics Report");
        titleCell.setCellStyle(createTitleStyle(workbook));

        rowNum++;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Row periodRow = sheet.createRow(rowNum++);
        periodRow.createCell(0).setCellValue("Report Period:");
        periodRow.createCell(1).setCellValue(startDate.format(formatter) + " to " + endDate.format(formatter));

        Row generatedRow = sheet.createRow(rowNum++);
        generatedRow.createCell(0).setCellValue("Generated:");
        Cell dateCell = generatedRow.createCell(1);
        dateCell.setCellValue(LocalDateTime.now().format(formatter));
        dateCell.setCellStyle(dateStyle);

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        Cell metricHeader = headerRow.createCell(0);
        metricHeader.setCellValue("Metric");
        metricHeader.setCellStyle(headerStyle);

        Cell valueHeader = headerRow.createCell(1);
        valueHeader.setCellValue("Value");
        valueHeader.setCellStyle(headerStyle);

        Map<String, Object> summary = (Map<String, Object>) analyticsData.get("summary");

        createDataRow(sheet, rowNum++, "Total Users", summary.get("totalUsers"));
        createDataRow(sheet, rowNum++, "Total Sessions", summary.get("totalSessions"));
        createDataRow(sheet, rowNum++, "Average Bounce Rate",
                String.format("%.2f%%", summary.get("avgBounceRate")));
        createDataRow(sheet, rowNum++, "Average Session Duration",
                String.format("%.2f seconds", summary.get("avgSessionDuration")));
        createDataRow(sheet, rowNum++, "Total Page Views", summary.get("totalPageViews"));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDailyMetricsSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Daily Metrics");
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);

        String[] headers = {"Date", "Active Users", "Sessions", "Bounce Rate",
                "Avg Session Duration", "Page Views"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Map<String, Object>> dailyMetrics =
                (List<Map<String, Object>>) analyticsData.get("dailyMetrics");

        for (Map<String, Object> metric : dailyMetrics) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) metric.get("date"));
            row.createCell(1).setCellValue(((Number) metric.get("activeUsers")).longValue());
            row.createCell(2).setCellValue(((Number) metric.get("sessions")).longValue());
            row.createCell(3).setCellValue(((Number) metric.get("bounceRate")).doubleValue());
            row.createCell(4).setCellValue(((Number) metric.get("averageSessionDuration")).doubleValue());
            row.createCell(5).setCellValue(((Number) metric.get("pageViews")).longValue());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createGeographicSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Geographic Distribution");
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);

        Cell countryHeader = headerRow.createCell(0);
        countryHeader.setCellValue("Country");
        countryHeader.setCellStyle(headerStyle);

        Cell usersHeader = headerRow.createCell(1);
        usersHeader.setCellValue("Users");
        usersHeader.setCellStyle(headerStyle);

        Map<String, Integer> countryData = (Map<String, Integer>) analyticsData.get("countryData");

        countryData.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> {
                    Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(entry.getValue());
                });

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDeviceSheet(Workbook workbook, Map<String, Object> analyticsData) {
        Sheet sheet = workbook.createSheet("Device Distribution");
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);

        Cell deviceHeader = headerRow.createCell(0);
        deviceHeader.setCellValue("Device Type");
        deviceHeader.setCellStyle(headerStyle);

        Cell usersHeader = headerRow.createCell(1);
        usersHeader.setCellValue("Users");
        usersHeader.setCellStyle(headerStyle);

        Map<String, Integer> deviceData = (Map<String, Integer>) analyticsData.get("deviceData");

        deviceData.forEach((device, users) -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(device);
            row.createCell(1).setCellValue(users);
        });

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value.toString());
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }
}