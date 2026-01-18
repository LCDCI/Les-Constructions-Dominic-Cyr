package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private GoogleAnalyticsService googleAnalyticsService;

    @Autowired
    private PDFReportGenerator pdfReportGenerator;

    @Autowired
    private XLSXReportGenerator xlsxReportGenerator;

    @Autowired
    private AnalyticsReportRepository reportRepository;

    @Value("${files.service.base-url}")
    private String filesServiceBaseUrl;

    @Value("${reports.storage.base-path}")
    private String storageBasePath;

    private final RestTemplate restTemplate = new RestTemplate();

    public AnalyticsReport generateReport(String ownerId, String reportType,
                                          String fileFormat, LocalDateTime startDate,
                                          LocalDateTime endDate) {

        // 1. Fetch data from Google Analytics
        Map<String, Object> analyticsData = googleAnalyticsService.fetchAnalyticsData(startDate, endDate, reportType);

        byte[] reportContent;
        String contentType;
        String fileExtension;

        try {
            // 2. Generate the physical file
            if ("PDF".equalsIgnoreCase(fileFormat)) {
                reportContent = pdfReportGenerator.generatePDFReport(analyticsData, startDate, endDate);
                contentType = "application/pdf";
                fileExtension = "pdf";
            } else if ("XLSX".equalsIgnoreCase(fileFormat)) {
                reportContent = xlsxReportGenerator.generateXLSXReport(analyticsData, startDate, endDate);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileExtension = "xlsx";
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
            }

            // 3. Prepare storage path and upload to Go Service
            String fileName = String.format("%s_%s_%s.%s",
                    reportType,
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().toString().replace(":", "-"),
                    fileExtension);

            String objectKey = storageBasePath + fileName;
            String uploadedKey = uploadToStorage(reportContent, objectKey, contentType);

            // 4. Create Database Record
            AnalyticsReport report = new AnalyticsReport();
            report.setOwnerId(ownerId);
            report.setReportType(reportType);
            report.setFileFormat(fileFormat);
            report.setFileKey(uploadedKey);
            report.setFileSize((long) reportContent.length);
            report.setGenerationTimestamp(LocalDateTime.now());
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setStatus("COMPLETED");

            // 5. Build Metadata safely (Fixed TreeMap vs List casting issue)
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("summary", analyticsData.get("summary"));

            // Get record count by checking if it's a Map (TreeMap) or a Collection (List)
            Object dailyMetricsObj = analyticsData.get("dailyMetrics");
            int recordCount = 0;
            if (dailyMetricsObj instanceof Map) {
                recordCount = ((Map<?, ?>) dailyMetricsObj).size();
            } else if (dailyMetricsObj instanceof Collection) {
                recordCount = ((Collection<?>) dailyMetricsObj).size();
            }

            metadata.put("recordCount", recordCount);
            report.setMetadata(metadata);

            return reportRepository.save(report);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report");
        }
    }

    private String uploadToStorage(byte[] content, String objectKey, String contentType) {
        String uploadUrl = filesServiceBaseUrl + "/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return objectKey.substring(objectKey.lastIndexOf("/") + 1);
            }
        });
        body.add("objectKey", objectKey);
        body.add("contentType", contentType);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Go Service error: " + response.getStatusCode());
            }

            Object returnedKey = response.getBody().get("objectKey");
            if (returnedKey == null) {
                throw new RuntimeException("Go Service did not return an objectKey (UUID)");
            }

            return returnedKey.toString();
        } catch (Exception e) {
            throw new RuntimeException("Communication with Go File Service failed: " + e.getMessage());
        }
    }

    public byte[] downloadReport(UUID reportId, String ownerId) {
        AnalyticsReport report = getReportById(reportId, ownerId);
        String downloadUrl = filesServiceBaseUrl + "/files/" + report.getFileKey();

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Go Service download failed: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download report from storage: " + e.getMessage());
        }
    }

    public Page<AnalyticsReport> getReportsByOwner(String ownerId, Pageable pageable) {
        return reportRepository.findByOwnerIdOrderByGenerationTimestampDesc(ownerId, pageable);
    }

    public AnalyticsReport getReportById(UUID reportId, String ownerId) {
        AnalyticsReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        if (!report.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Access denied: Report does not belong to user");
        }

        return report;
    }

    @Transactional
    public void deleteReport(UUID reportId, String ownerId) {
        AnalyticsReport report = getReportById(reportId, ownerId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("deletedBy", ownerId);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            String goFileId = report.getFileKey();

            restTemplate.exchange(
                    filesServiceBaseUrl + "/files/" + goFileId,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class
            );
        } catch (Exception e) {
            System.err.println("Storage cleanup failed for file ");
        }

        reportRepository.delete(report);
    }
}