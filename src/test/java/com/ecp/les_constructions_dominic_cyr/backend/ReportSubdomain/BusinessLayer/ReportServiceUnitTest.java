package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportServiceUnitTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private GoogleAnalyticsService googleAnalyticsService;
    @Mock
    private PDFReportGenerator pdfReportGenerator;
    @Mock
    private XLSXReportGenerator xlsxReportGenerator;
    @Mock
    private AnalyticsReportRepository reportRepository;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(reportService, "filesServiceBaseUrl", "http://localhost:8082");
        ReflectionTestUtils.setField(reportService, "storageBasePath", "reports/");
        ReflectionTestUtils.setField(reportService, "restTemplate", restTemplate);
    }

    @Test
    void generateReport_PDF_Success() throws Exception {
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("summary", Map.of("totalUsers", 10L));
        analyticsData.put("dailyMetrics", Map.of("2023-01-01", Map.of()));

        when(googleAnalyticsService.fetchAnalyticsData(any(), any(), any())).thenReturn(analyticsData);
        when(pdfReportGenerator.generatePDFReport(any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        Map<String, Object> uploadResponse = new HashMap<>();
        uploadResponse.put("objectKey", "uuid-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(uploadResponse, HttpStatus.OK));

        when(reportRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        AnalyticsReport result = reportService.generateReport("user1", "AUDIT", "PDF", LocalDateTime.now(), LocalDateTime.now());

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("uuid-key", result.getFileKey());
    }

    @Test
    void generateReport_UnsupportedFormat_ThrowsException() {
        assertThrows(RuntimeException.class, () ->
                reportService.generateReport("u1", "A", "GIF", LocalDateTime.now(), LocalDateTime.now())
        );
    }

    @Test
    void downloadReport_Success() {
        UUID id = UUID.randomUUID();
        AnalyticsReport report = new AnalyticsReport();
        report.setOwnerId("user1");
        report.setFileKey("key");

        when(reportRepository.findById(id)).thenReturn(Optional.of(report));
        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(new byte[]{1}, HttpStatus.OK));

        byte[] result = reportService.downloadReport(id, "user1");
        assertNotNull(result);
    }

    @Test
    void getReportById_SecurityException() {
        UUID id = UUID.randomUUID();
        AnalyticsReport report = new AnalyticsReport();
        report.setOwnerId("other");

        when(reportRepository.findById(id)).thenReturn(Optional.of(report));

        assertThrows(SecurityException.class, () -> reportService.getReportById(id, "user1"));
    }

    @Test
    void deleteReport_Success() {
        UUID id = UUID.randomUUID();
        AnalyticsReport report = new AnalyticsReport();
        report.setOwnerId("user1");
        report.setFileKey("key");

        when(reportRepository.findById(id)).thenReturn(Optional.of(report));

        reportService.deleteReport(id, "user1");

        verify(reportRepository, times(1)).delete(report);
    }

    @Test
    void getReportsByOwner_Success() {
        Page<AnalyticsReport> page = new PageImpl<>(Collections.emptyList());
        when(reportRepository.findByOwnerIdOrderByGenerationTimestampDesc(anyString(), any())).thenReturn(page);

        Page<AnalyticsReport> result = reportService.getReportsByOwner("user1", Pageable.unpaged());
        assertNotNull(result);
    }
}