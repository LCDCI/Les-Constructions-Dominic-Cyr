package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleAnalyticsService {

    @Value("${google.analytics.property-id}")
    private String propertyId;

    @Value("${google.analytics.credentials-path}")
    private String credentialsPath;

    @Value("${GOOGLE_ANALYTICS_CREDENTIALS:}")
    private String credentialsJson;


    private long safeParseLong(String value) {
        try {
            return (value == null || value.isEmpty()) ? 0L : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private double safeParseDouble(String value) {
        try {
            return (value == null || value.isEmpty()) ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Map<String, Object> fetchAnalyticsData(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        try {
            GoogleCredentials credentials;

            // Check if credentials are provided via environment variable (production)
            if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
                try (InputStream is = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(is)
                            .createScoped(Collections.singletonList("https://www.googleapis.com/auth/analytics.readonly"));
                }
            } else {
                // Fall back to reading from file (local development)
                try (FileInputStream fis = new FileInputStream(credentialsPath)) {
                    credentials = GoogleCredentials.fromStream(fis)
                            .createScoped(Collections.singletonList("https://www.googleapis.com/auth/analytics.readonly"));
                }
            }

            BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (BetaAnalyticsDataClient analyticsData = BetaAnalyticsDataClient.create(settings)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                RunReportRequest.Builder requestBuilder = RunReportRequest.newBuilder()
                        .setProperty("properties/" + propertyId)
                        .addDateRanges(DateRange.newBuilder()
                                .setStartDate(startDate.format(formatter))
                                .setEndDate(endDate.format(formatter)))
                        .addMetrics(Metric.newBuilder().setName("activeUsers"))
                        .addMetrics(Metric.newBuilder().setName("sessions"))
                        .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                        .addMetrics(Metric.newBuilder().setName("bounceRate"))
                        .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                        .addMetrics(Metric.newBuilder().setName("engagementRate"))
                        .addMetrics(Metric.newBuilder().setName("scrolledUsers"))
                        .addDimensions(Dimension.newBuilder().setName("date"))
                        .addDimensions(Dimension.newBuilder().setName("pagePath"))
                        .addDimensions(Dimension.newBuilder().setName("sessionSource"))
                        .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                        .addDimensions(Dimension.newBuilder().setName("city"));

                RunReportResponse response = analyticsData.runReport(requestBuilder.build());
                return processAnalyticsResponse(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Google Analytics data", e);
        }
    }

    private Map<String, Object> processAnalyticsResponse(RunReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        Map<String, Map<String, Object>> dailyAggregation = new TreeMap<>();
        Map<String, Integer> cityData = new HashMap<>();
        Map<String, Integer> sourceData = new HashMap<>();
        Map<String, Long> pageViewsData = new HashMap<>();
        Map<String, Integer> deviceData = new HashMap<>();

        long totalUsers = 0;
        long totalSessions = 0;
        long totalPageViews = 0;
        long totalScrolledUsers = 0;
        double totalBounceRate = 0;
        double sumSessionDuration = 0;

        for (Row row : response.getRowsList()) {
            String date = row.getDimensionValues(0).getValue();
            String pagePath = row.getDimensionValues(1).getValue();
            String source = row.getDimensionValues(2).getValue();
            String device = row.getDimensionValues(3).getValue();
            String city = row.getDimensionValues(4).getValue();

            long users = safeParseLong(row.getMetricValues(0).getValue());
            long sessions = safeParseLong(row.getMetricValues(1).getValue());
            long pageViews = safeParseLong(row.getMetricValues(2).getValue());
            double bounceRate = safeParseDouble(row.getMetricValues(3).getValue());
            double duration = safeParseDouble(row.getMetricValues(4).getValue());
            long scrolled = safeParseLong(row.getMetricValues(6).getValue());

            dailyAggregation.computeIfAbsent(date, k -> new HashMap<>(Map.of("activeUsers", 0L, "sessions", 0L)));
            dailyAggregation.get(date).merge("activeUsers", users, (o, v) -> ((Number)o).longValue() + ((Number)v).longValue());
            dailyAggregation.get(date).merge("sessions", sessions, (o, v) -> ((Number)o).longValue() + ((Number)v).longValue());
            cityData.merge(city, (int) users, Integer::sum);
            sourceData.merge(source, (int) users, Integer::sum);
            pageViewsData.merge(pagePath, pageViews, Long::sum);
            deviceData.merge(device, (int) users, Integer::sum);

            totalUsers += users;
            totalSessions += sessions;
            totalPageViews += pageViews;
            totalScrolledUsers += scrolled;
            totalBounceRate += bounceRate;
            sumSessionDuration += duration;
        }

        int rowCount = response.getRowsCount();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("totalSessions", totalSessions);
        summary.put("totalPageViews", totalPageViews);
        summary.put("avgBounceRate", rowCount > 0 ? totalBounceRate / rowCount : 0);
        summary.put("avgSessionDuration", rowCount > 0 ? sumSessionDuration / rowCount : 0);
        summary.put("scrollRate", totalUsers > 0 ? ((double) totalScrolledUsers / totalUsers) * 100 : 0);

        data.put("summary", summary);
        data.put("dailyMetrics", dailyAggregation);
        data.put("cityData", cityData);
        data.put("sourceData", sourceData);
        data.put("pageViewsData", pageViewsData);
        data.put("deviceData", deviceData);

        data.put("projectAnalysis", performProjectInterestAnalysis(response.getRowsList()));
        data.put("businessInsights", calculateBusinessInsights(summary));

        return data;
    }

    private List<Map<String, Object>> performProjectInterestAnalysis(List<Row> rows) {
        List<Map<String, Object>> analysisResult = new ArrayList<>();
        Map<String, List<Row>> groupedByPage = rows.stream()
                .collect(Collectors.groupingBy(r -> r.getDimensionValues(1).getValue()));

        for (Map.Entry<String, List<Row>> entry : groupedByPage.entrySet()) {
            String path = entry.getKey();
            if (!path.contains("/projects") && !path.equals("/")) continue;

            List<Row> projectRows = entry.getValue();

            long pViews = projectRows.stream().mapToLong(r -> safeParseLong(r.getMetricValues(2).getValue())).sum();
            long pUsers = projectRows.stream().mapToLong(r -> safeParseLong(r.getMetricValues(0).getValue())).sum();
            double avgDur = projectRows.stream().mapToDouble(r -> safeParseDouble(r.getMetricValues(4).getValue())).average().orElse(0);
            long scrolled = projectRows.stream().mapToLong(r -> safeParseLong(r.getMetricValues(6).getValue())).sum();

            double scrollFactor = pUsers > 0 ? (double) scrolled / pUsers : 0;
            double durationFactor = Math.min(avgDur / 180.0, 1.0);
            double volumeFactor = Math.min((double) pViews / 500.0, 1.0);

            double piiScore = (durationFactor * 0.4) + (scrollFactor * 0.4) + (volumeFactor * 0.2);

            double volatility = calculateVolatility(projectRows);

            Map<String, Object> projectMetrics = new HashMap<>();
            projectMetrics.put("path", path);
            projectMetrics.put("piiScore", piiScore * 100);
            projectMetrics.put("volatility", volatility);
            projectMetrics.put("engagementLevel", piiScore > 0.7 ? "HOT" : piiScore > 0.4 ? "WARM" : "COLD");

            analysisResult.add(projectMetrics);
        }

        analysisResult.sort((a, b) -> Double.compare((double) b.get("piiScore"), (double) a.get("piiScore")));
        return analysisResult.stream().limit(10).collect(Collectors.toList());
    }

    private double calculateVolatility(List<Row> rows) {
        List<Long> dailySessions = rows.stream()
                .map(r -> safeParseLong(r.getMetricValues(1).getValue()))
                .collect(Collectors.toList());

        if (dailySessions.size() < 2) return 0.0;

        double mean = dailySessions.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = dailySessions.stream()
                .mapToDouble(s -> Math.pow(s - mean, 2))
                .sum() / dailySessions.size();

        return Math.sqrt(variance);
    }

    private Map<String, Object> calculateBusinessInsights(Map<String, Object> summary) {
        Map<String, Object> insights = new HashMap<>();
        double scrollRate = ((Number) summary.get("scrollRate")).doubleValue();
        double bounceRate = ((Number) summary.get("avgBounceRate")).doubleValue();

        String intent = (scrollRate > 40 && bounceRate < 50) ? "HIGH" : "PASSIVE";
        insights.put("readerIntent", intent);
        insights.put("recommendation", intent.equals("HIGH")
                ? "Users are deeply engaged. Increase Call-to-Actions on project pages."
                : "Users are skimming. Use more visual high-level project summaries.");

        return insights;
    }
}