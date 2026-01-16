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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartUtils;
import org.springframework.stereotype.Service;

import java.awt.BasicStroke;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PDFReportGenerator {

    // Theme Colors for Les Constructions Dominic Cyr (Slate Blue/Gray)
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(52, 73, 94);
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(41, 128, 185);

    public byte[] generatePDFReport(Map<String, Object> analyticsData,
                                    LocalDateTime startDate,
                                    LocalDateTime endDate) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        addHeader(document, startDate, endDate);
        addSummarySection(document, analyticsData);
        addBusinessInsightsSection(document, analyticsData); // NEW: Insight logic
        addCharts(document, analyticsData);
        addDetailedMetrics(document, analyticsData);
        addFooter(document);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, LocalDateTime startDate, LocalDateTime endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

        document.add(new Paragraph("LES CONSTRUCTIONS DOMINIC CYR INC.")
                .setFontColor(PRIMARY_COLOR)
                .setBold()
                .setFontSize(10));

        Paragraph title = new Paragraph("Business Analytics Intelligence Report")
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(5);

        Paragraph period = new Paragraph(String.format("Reporting Period: %s — %s",
                startDate.format(formatter), endDate.format(formatter)))
                .setFontSize(11)
                .setItalic()
                .setMarginBottom(20);

        document.add(title);
        document.add(period);
    }

    private void addSummarySection(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> summary = (Map<String, Object>) analyticsData.get("summary");

        document.add(new Paragraph("Executive Key Performance Indicators")
                .setFontSize(14).setBold().setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                .setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Core Metric", true));
        table.addHeaderCell(createCell("Current Total", true));

        table.addCell(createCell("Total Reach (Active Users)", false));
        table.addCell(createCell(String.valueOf(summary.get("totalUsers")), false));

        table.addCell(createCell("Engagement (Total Sessions)", false));
        table.addCell(createCell(String.valueOf(summary.get("totalSessions")), false));

        table.addCell(createCell("Retention (Bounce Rate)", false));
        table.addCell(createCell(String.format("%.2f%%", summary.get("avgBounceRate")), false));

        table.addCell(createCell("Content Volume (Page Views)", false));
        table.addCell(createCell(String.valueOf(summary.get("totalPageViews")), false));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addBusinessInsightsSection(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> insights = (Map<String, Object>) analyticsData.get("businessInsights");
        if (insights == null) return;

        document.add(new Paragraph("Strategic Business Insights")
                .setFontSize(14).setBold().setFontColor(ACCENT_COLOR).setMarginBottom(10));

        Div insightBox = new Div()
                .setBackgroundColor(new DeviceRgb(245, 247, 250))
                .setPadding(10)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(ColorConstants.LIGHT_GRAY, 1));

        insightBox.add(new Paragraph("User Acquisition Rate: ").setBold()
                .add(new Text((String) insights.get("acquisitionRate")).setBold().setFontColor(ACCENT_COLOR)));

        insightBox.add(new Paragraph("Strategic Recommendation: ").setBold()
                .add(new Text((String) insights.get("retentionDescription")).setItalic()));

        insightBox.add(new Paragraph("Content Stickiness: ").setBold()
                .add(new Text((String) insights.get("contentStickiness"))));

        document.add(insightBox);
        document.add(new Paragraph("\n"));
    }

    private void addCharts(Document document, Map<String, Object> analyticsData) throws Exception {
        document.add(new Paragraph("Visual Data Trends").setFontSize(14).setBold().setMarginBottom(10));

        List<Map<String, Object>> dailyMetrics = (List<Map<String, Object>>) analyticsData.get("dailyMetrics");
        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();

        for (Map<String, Object> metric : dailyMetrics) {
            String date = (String) metric.get("date");
            lineDataset.addValue((Number) metric.get("activeUsers"), "Active Users", date);
            lineDataset.addValue((Number) metric.get("sessions"), "Sessions", date);
        }

        JFreeChart lineChart = ChartFactory.createLineChart("User Activity Trends", "Timeline", "Volume", lineDataset);

        // HD Styling: Fixes the "Look like shit" issue
        lineChart.setBackgroundPaint(java.awt.Color.WHITE);
        CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setRangeGridlinePaint(java.awt.Color.LIGHT_GRAY);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(3.0f)); // Thicker lines
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        plot.setRenderer(renderer);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        // Higher resolution: 1000x600 instead of 500x300
        ChartUtils.writeChartAsPNG(chartStream, lineChart, 1000, 600);

        Image chartImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(chartStream.toByteArray()))
                .setWidth(UnitValue.createPercentValue(100))
                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

        document.add(chartImage);
        document.add(new AreaBreak()); // Start detailed tables on a fresh page
    }

    private void addDetailedMetrics(Document document, Map<String, Object> analyticsData) {
        document.add(new Paragraph("Geographic Market Distribution").setFontSize(14).setBold().setMarginBottom(10));

        Map<String, Integer> countryData = (Map<String, Integer>) analyticsData.get("countryData");
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1})).setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(createCell("Market / Country", true));
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
        document.add(new Paragraph("PROPRIETARY DATA — LES CONSTRUCTIONS DOMINIC CYR INC.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content).setPadding(5));
        if (isHeader) {
            cell.setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE).setBold();
        }
        return cell;
    }
}