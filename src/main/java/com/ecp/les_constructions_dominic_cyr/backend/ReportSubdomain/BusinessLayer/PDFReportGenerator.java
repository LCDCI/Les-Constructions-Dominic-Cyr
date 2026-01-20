package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.SolidBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
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

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(44, 62, 80); // Midnight Blue
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(192, 57, 43); // Construction Red
    private static final DeviceRgb SHADE_COLOR = new DeviceRgb(245, 247, 250);

    public byte[] generatePDFReport(Map<String, Object> analyticsData,
                                    LocalDateTime startDate,
                                    LocalDateTime endDate) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document document = new Document(pdf);

        addHeader(document, startDate, endDate);
        addSummarySection(document, analyticsData);
        addBusinessInsightsSection(document, analyticsData);

        document.add(new AreaBreak());
        addProjectInterestLeaderboard(document, (List<Map<String, Object>>) analyticsData.get("projectAnalysis"));

        document.add(new AreaBreak());
        addGeographicIntelligence(document, (Map<String, Integer>) analyticsData.get("cityData"));
        addTrafficSources(document, (Map<String, Integer>) analyticsData.get("sourceData"));

        document.add(new AreaBreak());
        addActivityCharts(document, analyticsData);

        document.add(new AreaBreak());
        addStrategicRoadmap(document, analyticsData);

        addFooter(document);
        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document document, LocalDateTime startDate, LocalDateTime endDate) {
        document.add(new Paragraph("LES CONSTRUCTIONS DOMINIC CYR INC.")
                .setFontColor(PRIMARY_COLOR).setBold().setFontSize(10));

        document.add(new Paragraph("Master Business Intelligence Audit")
                .setFontSize(24).setBold().setMarginBottom(0));

        document.add(new Paragraph(String.format("Data Integrity Period: %s to %s",
                startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                .setFontSize(10).setItalic().setMarginBottom(20));
    }

    private void addSummarySection(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> summary = (Map<String, Object>) analyticsData.get("summary");

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2})).useAllAvailableWidth();
        table.addHeaderCell(createCell("Executive Metric", true));
        table.addHeaderCell(createCell("Computed Value", true));

        table.addCell(createCell("Global Reach (Users)", false));
        table.addCell(createCell(String.valueOf(summary.get("totalUsers")), false));
        table.addCell(createCell("Engagement Density (Sessions)", false));
        table.addCell(createCell(String.valueOf(summary.get("totalSessions")), false));
        table.addCell(createCell("Reader Retention (Scroll Rate)", false));
        table.addCell(createCell(String.format("%.2f%%", summary.get("scrollRate")), false));

        document.add(table.setMarginBottom(15));
    }

    private void addProjectInterestLeaderboard(Document document, List<Map<String, Object>> projectAnalysis) {
        document.add(new Paragraph("Algorithmic Project Interest Index (PII)")
                .setFontSize(16).setBold().setFontColor(PRIMARY_COLOR));

        document.add(new Paragraph("This section represents a custom data manipulation problem. We calculate 'Interest' by weighting Scroll Depth (40%), Duration (40%), and View Volume (20%) across thousands of data points.")
                .setFontSize(9).setItalic().setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2, 2})).useAllAvailableWidth();
        table.addHeaderCell(createCell("Construction Project Path", true));
        table.addHeaderCell(createCell("PII Score", true));
        table.addHeaderCell(createCell("Volatility", true));
        table.addHeaderCell(createCell("Status", true));

        for (Map<String, Object> project : projectAnalysis) {
            table.addCell(createCell((String) project.get("path"), false));
            table.addCell(createCell(String.format("%.1f", project.get("piiScore")), false));
            table.addCell(createCell(String.format("%.2f", project.get("volatility")), false));

            String level = (String) project.get("engagementLevel");
            Cell statusCell = createCell(level, false);
            if (level.equals("HOT")) statusCell.setFontColor(ACCENT_COLOR).setBold();
            table.addCell(statusCell);
        }
        document.add(table);
    }

    private void addGeographicIntelligence(Document document, Map<String, Integer> cityData) {
        document.add(new Paragraph("Regional Intelligence (Quebec / Canada)")
                .setFontSize(14).setBold().setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
        table.addHeaderCell(createCell("Top Interest Cities", true));
        table.addHeaderCell(createCell("Active Leads", true));

        cityData.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(8)
                .forEach(entry -> {
                    table.addCell(createCell(entry.getKey(), false));
                    table.addCell(createCell(String.valueOf(entry.getValue()), false));
                });
        document.add(table.setMarginBottom(20));
    }

    private void addTrafficSources(Document document, Map<String, Integer> sourceData) {
        document.add(new Paragraph("Acquisition Attribution Model").setFontSize(14).setBold());
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1})).useAllAvailableWidth();
        sourceData.forEach((k, v) -> {
            table.addCell(createCell(k, false));
            table.addCell(createCell(String.valueOf(v), false));
        });
        document.add(table);
    }

    private void addActivityCharts(Document document, Map<String, Object> analyticsData) throws Exception {
        document.add(new Paragraph("Historical Engagement Matrix").setFontSize(14).setBold().setMarginBottom(10));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Map<String, Object>> daily = (Map<String, Map<String, Object>>) analyticsData.get("dailyMetrics");

        daily.forEach((date, metrics) -> {
            dataset.addValue((Number) metrics.get("activeUsers"), "Users", date);
        });

        JFreeChart chart = ChartFactory.createLineChart("User Growth Trends", "Date", "Users", dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(java.awt.Color.WHITE);

        ByteArrayOutputStream chartOut = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOut, chart, 1000, 500);
        document.add(new Image(com.itextpdf.io.image.ImageDataFactory.create(chartOut.toByteArray()))
                .setWidth(UnitValue.createPercentValue(100)) // This makes it full width
                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER));
    }

    private void addStrategicRoadmap(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> insights = (Map<String, Object>) analyticsData.get("businessInsights");

        document.add(new Paragraph("Strategic Recommendations & Action Plan")
                .setFontSize(16).setBold().setFontColor(PRIMARY_COLOR).setMarginTop(20));

        Div div = new Div().setPadding(15).setBackgroundColor(SHADE_COLOR).setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));

        div.add(new Paragraph("1. Market Position: ").setBold().add(new Text("Current intent is " + insights.get("readerIntent"))));
        div.add(new Paragraph("2. Optimization: ").setBold().add(new Text((String) insights.get("recommendation"))));
        div.add(new Paragraph("3. Data Note: ").setBold().add(new Text("Volatility analysis suggests prioritizing projects with stable, high-PII scores for Facebook marketing spend.")));

        document.add(div);
    }

    private void addBusinessInsightsSection(Document document, Map<String, Object> analyticsData) {
        Map<String, Object> insights = (Map<String, Object>) analyticsData.get("businessInsights");
        document.add(new Paragraph("Executive Synthesis: ").setBold()
                .add(new Text("Based on aggregated data, the current audience shows " + insights.get("readerIntent") + " intent levels.")));
    }

    private Cell createCell(String content, boolean isHeader) {
        Cell cell = new Cell().add(new Paragraph(content).setPadding(5).setFontSize(9));
        if (isHeader) {
            cell.setBackgroundColor(PRIMARY_COLOR).setFontColor(ColorConstants.WHITE).setBold();
        }
        return cell;
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n\nPROPRIETARY AND CONFIDENTIAL — GENERATED FOR LES CONSTRUCTIONS DOMINIC CYR")
                .setFontSize(7).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
    }
}