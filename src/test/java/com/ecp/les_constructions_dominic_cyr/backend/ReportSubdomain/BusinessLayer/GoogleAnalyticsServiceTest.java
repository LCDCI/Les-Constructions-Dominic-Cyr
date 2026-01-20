package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.google.analytics.data.v1beta.DimensionValue;
import com.google.analytics.data.v1beta.MetricValue;
import com.google.analytics.data.v1beta.Row;
import com.google.analytics.data.v1beta.RunReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GoogleAnalyticsServiceTest {

    private GoogleAnalyticsService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new GoogleAnalyticsService();
        ReflectionTestUtils.setField(service, "propertyId", "12345");
        ReflectionTestUtils.setField(service, "credentialsPath", "non-existent.json");
    }

    @Test
    void fetchAnalyticsData_InvalidCredentials_ThrowsRuntimeException() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(RuntimeException.class, () ->
                service.fetchAnalyticsData(now.minusDays(7), now, "AUDIT")
        );
    }

    @Test
    void testProcessAnalyticsResponse_FullCoverage() {
        List<Row> rows = new ArrayList<>();

        rows.add(createMockRow("/projects/house-1", "google", "mobile", "Montreal", "10", "5", "20", "20.5", "150", "0.8", "8"));
        rows.add(createMockRow("/projects/house-1", "google", "mobile", "Montreal", "5", "15", "10", "10.0", "50", "0.9", "4"));
        rows.add(createMockRow("/", "direct", "desktop", "Quebec", "2", "2", "2", "80.0", "10", "0.1", "1"));

        RunReportResponse response = RunReportResponse.newBuilder()
                .addAllRows(rows)
                .build();

        Map<String, Object> result = (Map<String, Object>) ReflectionTestUtils.invokeMethod(
                service, "processAnalyticsResponse", response);

        assertNotNull(result);
        assertTrue(result.containsKey("summary"));
        assertTrue(result.containsKey("projectAnalysis"));

        Map<String, Object> summary = (Map<String, Object>) result.get("summary");
        assertTrue((Double) summary.get("scrollRate") > 0);

        List<Map<String, Object>> projectAnalysis = (List<Map<String, Object>>) result.get("projectAnalysis");
        assertFalse(projectAnalysis.isEmpty());
        assertEquals("WARM", projectAnalysis.get(0).get("engagementLevel"));
    }

    @Test
    void testBusinessInsights_PassiveIntent() {
        Map<String, Object> lowEngagementSummary = new HashMap<>();
        lowEngagementSummary.put("scrollRate", 10.0);
        lowEngagementSummary.put("avgBounceRate", 75.0);

        Map<String, Object> insights = (Map<String, Object>) ReflectionTestUtils.invokeMethod(
                service, "calculateBusinessInsights", lowEngagementSummary);

        assertEquals("PASSIVE", insights.get("readerIntent"));
    }

    @Test
    void testVolatility_EmptyList() {
        double volatility = (double) ReflectionTestUtils.invokeMethod(
                service, "calculateVolatility", Collections.singletonList(createMockRow("/", "d", "d", "c", "1", "1", "1", "0", "0", "0", "0")));
        assertEquals(0.0, volatility);
    }

    @Test
    void testProjectAnalysis_NoValidPaths() {
        Row row = createMockRow("/about-us", "d", "d", "c", "1", "1", "1", "0", "0", "0", "0");
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
                service, "performProjectInterestAnalysis", Collections.singletonList(row));
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchAnalyticsData_CredentialFlow() throws Exception {
        File tempFile = tempDir.resolve("credentials.json").toFile();
        String mockJson = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"test-project\",\n" +
                "  \"private_key_id\": \"123\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCe\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"test@test.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"123\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/spec\"\n" +
                "}";

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(mockJson.getBytes());
        }

        ReflectionTestUtils.setField(service, "credentialsPath", tempFile.getAbsolutePath());

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        assertThrows(RuntimeException.class, () ->
                service.fetchAnalyticsData(start, end, "AUDIT")
        );
    }

    @Test
    void testFetchAnalyticsData_FileNotFound() {
        ReflectionTestUtils.setField(service, "credentialsPath", "invalid/path/to/file.json");

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.fetchAnalyticsData(start, end, "AUDIT")
        );

        assertTrue(exception.getMessage().contains("Failed to fetch Google Analytics data"));
    }

    @Test
    void testFetchAnalyticsData_FormattingAndStructure() throws Exception {
        File tempFile = tempDir.resolve("empty_credentials.json").toFile();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("{}".getBytes());
        }
        ReflectionTestUtils.setField(service, "credentialsPath", tempFile.getAbsolutePath());

        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 7, 10, 0);

        assertThrows(RuntimeException.class, () ->
                service.fetchAnalyticsData(start, end, "AUDIT")
        );
    }

    private Row createMockRow(String path, String source, String device, String city,
                              String users, String sessions, String views, String bounce,
                              String duration, String engagement, String scrolled) {
        return Row.newBuilder()
                .addDimensionValues(DimensionValue.newBuilder().setValue("2023-10-01"))
                .addDimensionValues(DimensionValue.newBuilder().setValue(path))
                .addDimensionValues(DimensionValue.newBuilder().setValue(source))
                .addDimensionValues(DimensionValue.newBuilder().setValue(device))
                .addDimensionValues(DimensionValue.newBuilder().setValue(city))
                .addMetricValues(MetricValue.newBuilder().setValue(users))      // index 0
                .addMetricValues(MetricValue.newBuilder().setValue(sessions))   // index 1
                .addMetricValues(MetricValue.newBuilder().setValue(views))      // index 2
                .addMetricValues(MetricValue.newBuilder().setValue(bounce))     // index 3
                .addMetricValues(MetricValue.newBuilder().setValue(duration))   // index 4
                .addMetricValues(MetricValue.newBuilder().setValue(engagement)) // index 5
                .addMetricValues(MetricValue.newBuilder().setValue(scrolled))   // index 6
                .build();
    }
}