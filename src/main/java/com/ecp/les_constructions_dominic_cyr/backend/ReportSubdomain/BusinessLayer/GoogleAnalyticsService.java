package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
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

    /**
     * Fetches raw data from Google Analytics and processes it into actionable business insights.
     */
    public Map<String, Object> fetchAnalyticsData(LocalDateTime startDate, LocalDateTime endDate, String reportType) {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            ).createScoped(Collections.singletonList("https://www.googleapis.com/auth/analytics.readonly"));

            BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (BetaAnalyticsDataClient analyticsData = BetaAnalyticsDataClient.create(settings)) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedStartDate = startDate.format(formatter);
                String formattedEndDate = endDate.format(formatter);

                RunReportRequest.Builder requestBuilder = RunReportRequest.newBuilder()
                        .setProperty("properties/" + propertyId)
                        .addDateRanges(DateRange.newBuilder()
                                .setStartDate(formattedStartDate)
                                .setEndDate(formattedEndDate))
                        // Core metrics used for summary and complex calculations
                        .addMetrics(Metric.newBuilder().setName("activeUsers"))
                        .addMetrics(Metric.newBuilder().setName("sessions"))
                        .addMetrics(Metric.newBuilder().setName("bounceRate"))
                        .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                        .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                        .addMetrics(Metric.newBuilder().setName("newUsers"))
                        // Core dimensions for segmentation
                        .addDimensions(Dimension.newBuilder().setName("date"))
                        .addDimensions(Dimension.newBuilder().setName("country"))
                        .addDimensions(Dimension.newBuilder().setName("deviceCategory"));

                // Add specialized dimensions based on report type
                if ("PROJECT_PERFORMANCE".equalsIgnoreCase(reportType)) {
                    requestBuilder.addDimensions(Dimension.newBuilder().setName("pagePath"));
                }

                RunReportResponse response = analyticsData.runReport(requestBuilder.build());
                return processAnalyticsResponse(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Google Analytics data", e);
        }
    }

    private Map<String, Object> processAnalyticsResponse(RunReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        // Intermediary structures for aggregation
        Map<String, Map<String, Object>> dailyAggregation = new TreeMap<>();
        Map<String, Integer> countryData = new HashMap<>();
        Map<String, Integer> deviceData = new HashMap<>();

        long totalUsers = 0;
        long totalNewUsers = 0;
        long totalSessions = 0;
        double totalBounceRate = 0;
        double totalSessionDuration = 0;
        long totalPageViews = 0;

        for (Row row : response.getRowsList()) {
            String date = row.getDimensionValues(0).getValue();
            String country = row.getDimensionValues(1).getValue();
            String device = row.getDimensionValues(2).getValue();

            long users = Long.parseLong(row.getMetricValues(0).getValue());
            long sessions = Long.parseLong(row.getMetricValues(1).getValue());
            double bounceRate = Double.parseDouble(row.getMetricValues(2).getValue());
            double sessionDuration = Double.parseDouble(row.getMetricValues(3).getValue());
            long pageViews = Long.parseLong(row.getMetricValues(4).getValue());
            long newUsers = Long.parseLong(row.getMetricValues(5).getValue());

            // 1. Aggregate daily metrics (prevents duplicate dates in charts)
            dailyAggregation.computeIfAbsent(date, k -> new HashMap<>(Map.of(
                    "activeUsers", 0L, "sessions", 0L, "pageViews", 0L
            )));
            dailyAggregation.get(date).merge("activeUsers", users, (old, val) -> (long)old + (long)val);
            dailyAggregation.get(date).merge("sessions", sessions, (old, val) -> (long)old + (long)val);
            dailyAggregation.get(date).merge("pageViews", pageViews, (old, val) -> (long)old + (long)val);

            // 2. Geographic and Device Tally
            countryData.merge(country, (int) users, Integer::sum);
            deviceData.merge(device, (int) users, Integer::sum);

            // 3. Totals for Summary
            totalUsers += users;
            totalNewUsers += newUsers;
            totalSessions += sessions;
            totalBounceRate += bounceRate;
            totalSessionDuration += sessionDuration;
            totalPageViews += pageViews;
        }

        int rowCount = response.getRowsCount();
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("totalNewUsers", totalNewUsers);
        summary.put("totalSessions", totalSessions);
        summary.put("avgBounceRate", rowCount > 0 ? totalBounceRate / rowCount : 0);
        summary.put("avgSessionDuration", rowCount > 0 ? totalSessionDuration / rowCount : 0);
        summary.put("totalPageViews", totalPageViews);

        // Transform daily map to ordered list for JFreeChart
        List<Map<String, Object>> dailyMetrics = dailyAggregation.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = e.getValue();
                    m.put("date", e.getKey());
                    return m;
                }).collect(Collectors.toList());

        data.put("summary", summary);
        data.put("dailyMetrics", dailyMetrics);
        data.put("countryData", countryData);
        data.put("deviceData", deviceData);

        // NEW: Complex Data Manipulation
        data.put("businessInsights", calculateBusinessInsights(summary, totalNewUsers));

        return data;
    }

    /**
     * Performs complex data manipulations to give the admin deep insights.
     */
    private Map<String, Object> calculateBusinessInsights(Map<String, Object> summary, long newUsers) {
        Map<String, Object> insights = new HashMap<>();

        long totalUsers = (long) summary.get("totalUsers");
        long pageViews = (long) summary.get("totalPageViews");
        long sessions = (long) summary.get("totalSessions");

        // Insight 1: User Retention vs Acquisition
        double acquisitionRate = totalUsers > 0 ? ((double) newUsers / totalUsers) * 100 : 0;
        insights.put("acquisitionRate", String.format("%.2f%%", acquisitionRate));
        insights.put("retentionDescription", acquisitionRate > 50
                ? "High new discovery. Focus on lead conversion."
                : "Strong returning audience. Focus on project updates.");

        // Insight 2: Content Stickiness (Pages per Session)
        double pagesPerSession = sessions > 0 ? (double) pageViews / sessions : 0;
        insights.put("contentStickiness", String.format("%.2f pages/session", pagesPerSession));

        // Insight 3: Engagement Efficiency
        double avgDuration = (double) summary.get("avgSessionDuration");
        String efficiencyRating = (avgDuration > 120 && pagesPerSession > 2) ? "OPTIMAL" : "LOW";
        insights.put("engagementEfficiency", efficiencyRating);

        return insights;
    }
}