package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartUtils;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PDFReportGenerator {

    public byte[] generatePDFReport(Map<String, Object> analyticsData,
                                    LocalDateTime startDate,
                                    LocalDateTime endDate) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        addHeader(document, startDate, endDate);
        addSummarySection(document, analyticsData);
        addCharts(document, analyticsData);
        addDetailedMetrics(document, analyticsData);
        addFooter(document);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        Paragraph title = new Paragraph("Analytics Report")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        Paragraph period = new Paragraph(String.format("Period: %s - %s",
                startDate.format(formatter), endDate.format(formatter)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);

        Paragraph timestamp = new Paragraph(String.format("Generated: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        document.add(title);
        document.add(period);
        document.add(timestamp);
        document.add(new Paragraph("\n"));
    }

    private void addSummarySection(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> summary = (Map<String, Object>) analyticsData.get("summary");

        Paragraph sectionTitle = new Paragraph("Executive Summary")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Metric", true));
        table.addHeaderCell(createCell("Value", true));

        table.addCell(createCell("Total Users", false));
        table.addCell(createCell(String.valueOf(summary.get("totalUsers")), false));

        table.addCell(createCell("Total Sessions", false));
        table.addCell(createCell(String.valueOf(summary.get("totalSessions")), false));

        table.addCell(createCell("Average Bounce Rate", false));
        table.addCell(createCell(String.format("%.2f%%", summary.get("avgBounceRate")), false));

        table.addCell(createCell("Average Session Duration", false));
        table.addCell(createCell(String.format("%.2f seconds", summary.get("avgSessionDuration")), false));

        table.addCell(createCell("Total Page Views", false));
        table.addCell(createCell(String.valueOf(summary.get("totalPageViews")), false));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addCharts(Document document, Map<String, Object> analyticsData) throws Exception {
        Paragraph sectionTitle = new Paragraph("Visual Analytics")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        List<Map<String, Object>> dailyMetrics =
                (List<Map<String, Object>>) analyticsData.get("dailyMetrics");
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();

        for (Map<String, Object> metric : dailyMetrics) {
            String date = (String) metric.get("date");
            lineDataset.addValue((Number) metric.get("activeUsers"), "Active Users", date);
            lineDataset.addValue((Number) metric.get("sessions"), "Sessions", date);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "User Activity Over Time",
                "Date",
                "Count",
                lineDataset
        );

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, lineChart, 500, 300);

        com.itextpdf.io.image.ImageData imageData =
                com.itextpdf.io.image.ImageDataFactory.create(chartStream.toByteArray());
        Image chartImage = new Image(imageData);
        chartImage.setWidth(UnitValue.createPercentValue(80));
        chartImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        document.add(chartImage);
        document.add(new Paragraph("\n"));

        Map<String, Integer> deviceData = (Map<String, Integer>) analyticsData.get("deviceData");
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        deviceData.forEach(pieDataset::setValue);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Users by Device Type",
                pieDataset,
                true,
                true,
                false
        );

        ByteArrayOutputStream pieStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(pieStream, pieChart, 400, 300);

        com.itextpdf.io.image.ImageData pieImageData =
                com.itextpdf.io.image.ImageDataFactory.create(pieStream.toByteArray());
        Image pieImage = new Image(pieImageData);
        pieImage.setWidth(UnitValue.createPercentValue(60));
        pieImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        document.add(pieImage);
        document.add(new Paragraph("\n"));
    }

    private void addDetailedMetrics(Document document, Map<String, Object> analyticsData) {
        Paragraph sectionTitle = new Paragraph("Geographic Distribution")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        Map<String, Integer> countryData = (Map<String, Integer>) analyticsData.get("countryData");

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Country", true));
        table.addHeaderCell(createCell("Users", true));

        countryData.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .forEach(entry -> {
                    table.addCell(createCell(entry.getKey(), false));
                    table.addCell(createCell(String.valueOf(entry.getValue()), false));
                });

        document.add(table);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n\n"));
        Paragraph footer = new Paragraph("This report is confidential and intended for internal use only.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content));
        if (isHeader) {
            cell.setBackgroundColor(new DeviceRgb(52, 73, 94))
                    .setFontColor(ColorConstants.WHITE)
                    .setBold();
        }
        return cell;
    }
}