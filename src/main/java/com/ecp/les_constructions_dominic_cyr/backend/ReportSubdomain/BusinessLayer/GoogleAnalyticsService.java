package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.google.analytics.data.v1beta.*;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class GoogleAnalyticsService {

    @Value("${google.analytics.property-id}")
    private String propertyId;

    @Value("${google.analytics.credentials-path}")
    private String credentialsPath;

    public Map<String, Object> fetchAnalyticsData(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsPath)
            ).createScoped(Arrays.asList("https://www.googleapis.com/auth/analytics.readonly"));

            BetaAnalyticsDataSettings settings = BetaAnalyticsDataSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (BetaAnalyticsDataClient analyticsData = BetaAnalyticsDataClient.create(settings)) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedStartDate = startDate.format(formatter);
                String formattedEndDate = endDate.format(formatter);

                RunReportRequest request = RunReportRequest.newBuilder()
                        .setProperty("properties/" + propertyId)
                        .addDateRanges(DateRange.newBuilder()
                                .setStartDate(formattedStartDate)
                                .setEndDate(formattedEndDate))
                        .addMetrics(Metric.newBuilder().setName("activeUsers"))
                        .addMetrics(Metric.newBuilder().setName("sessions"))
                        .addMetrics(Metric.newBuilder().setName("bounceRate"))
                        .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                        .addMetrics(Metric.newBuilder().setName("screenPageViews"))
                        .addDimensions(Dimension.newBuilder().setName("date"))
                        .addDimensions(Dimension.newBuilder().setName("country"))
                        .addDimensions(Dimension.newBuilder().setName("deviceCategory"))
                        .build();

                RunReportResponse response = analyticsData.runReport(request);

                return processAnalyticsResponse(response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Google Analytics data", e);
        }
    }

    private Map<String, Object> processAnalyticsResponse(RunReportResponse response) {
        Map<String, Object> data = new HashMap<>();

        List<Map<String, Object>> dailyMetrics = new ArrayList<>();
        Map<String, Integer> countryData = new HashMap<>();
        Map<String, Integer> deviceData = new HashMap<>();

        long totalUsers = 0;
        long totalSessions = 0;
        double totalBounceRate = 0;
        double totalSessionDuration = 0;
        long totalPageViews = 0;

        for (Row row : response.getRowsList()) {
            Map<String, Object> rowData = new HashMap<>();

            String date = row.getDimensionValues(0).getValue();
            String country = row.getDimensionValues(1).getValue();
            String device = row.getDimensionValues(2).getValue();

            long users = Long.parseLong(row.getMetricValues(0).getValue());
            long sessions = Long.parseLong(row.getMetricValues(1).getValue());
            double bounceRate = Double.parseDouble(row.getMetricValues(2).getValue());
            double sessionDuration = Double.parseDouble(row.getMetricValues(3).getValue());
            long pageViews = Long.parseLong(row.getMetricValues(4).getValue());

            rowData.put("date", date);
            rowData.put("activeUsers", users);
            rowData.put("sessions", sessions);
            rowData.put("bounceRate", bounceRate);
            rowData.put("averageSessionDuration", sessionDuration);
            rowData.put("pageViews", pageViews);

            dailyMetrics.add(rowData);

            countryData.merge(country, (int) users, Integer::sum);
            deviceData.merge(device, (int) users, Integer::sum);

            totalUsers += users;
            totalSessions += sessions;
            totalBounceRate += bounceRate;
            totalSessionDuration += sessionDuration;
            totalPageViews += pageViews;
        }

        int rowCount = response.getRowsCount();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", totalUsers);
        summary.put("totalSessions", totalSessions);
        summary.put("avgBounceRate", rowCount > 0 ? totalBounceRate / rowCount : 0);
        summary.put("avgSessionDuration", rowCount > 0 ? totalSessionDuration / rowCount : 0);
        summary.put("totalPageViews", totalPageViews);

        data.put("summary", summary);
        data.put("dailyMetrics", dailyMetrics);
        data.put("countryData", countryData);
        data.put("deviceData", deviceData);

        return data;
    }
}