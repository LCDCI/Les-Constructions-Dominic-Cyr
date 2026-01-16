package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

        Map<String, Object> analyticsData = googleAnalyticsService.fetchAnalyticsData(startDate, endDate, reportType);

        byte[] reportContent;
        String contentType;
        String fileExtension;

        try {
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

            String fileName = String.format("%s_%s_%s.%s",
                    reportType,
                    UUID.randomUUID().toString(),
                    LocalDateTime.now().toString().replace(":", "-"),
                    fileExtension);

            String objectKey = storageBasePath + fileName;

            String uploadedKey = uploadToStorage(reportContent, objectKey, contentType);

            AnalyticsReport report = new AnalyticsReport();
            report.setOwnerId(ownerId); // Now matches: setOwnerId(String)
            report.setReportType(reportType);
            report.setFileFormat(fileFormat);
            report.setFileKey(uploadedKey);
            report.setFileSize((long) reportContent.length);
            report.setGenerationTimestamp(LocalDateTime.now());
            report.setStartDate(startDate);
            report.setEndDate(endDate);
            report.setStatus("COMPLETED");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("summary", analyticsData.get("summary"));
            metadata.put("recordCount", ((List<?>) analyticsData.get("dailyMetrics")).size());
            report.setMetadata(metadata);

            return reportRepository.save(report);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private String uploadToStorage(byte[] content, String objectKey, String contentType) {
        String uploadUrl = filesServiceBaseUrl + "/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        org.springframework.util.MultiValueMap<String, Object> body =
                new org.springframework.util.LinkedMultiValueMap<>();

        body.add("file", new org.springframework.core.io.ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return objectKey.substring(objectKey.lastIndexOf("/") + 1);
            }
        });
        body.add("objectKey", objectKey);
        body.add("contentType", contentType);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to upload report to storage");
        }

        return (String) response.getBody().get("objectKey");
    }

    // FIXED: Changed ownerId parameter type to String
    public byte[] downloadReport(UUID reportId, String ownerId) {
        AnalyticsReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Access denied");
        }

        String downloadUrl = filesServiceBaseUrl + "/files/" + report.getFileKey();

        ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to download report from storage");
        }

        return response.getBody();
    }

    // FIXED: Changed ownerId parameter type to String
    public Page<AnalyticsReport> getReportsByOwner(String ownerId, Pageable pageable) {
        return reportRepository.findByOwnerIdOrderByGenerationTimestampDesc(ownerId, pageable);
    }

    // FIXED: Changed ownerId parameter type to String
    public AnalyticsReport getReportById(UUID reportId, String ownerId) {
        AnalyticsReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Access denied");
        }

        return report;
    }

    public void deleteReport(UUID reportId, String ownerId) {
        AnalyticsReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        if (!report.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Access denied");
        }

        String deleteUrl = filesServiceBaseUrl + "/delete/" + report.getFileKey();
        restTemplate.delete(deleteUrl);

        reportRepository.delete(report);
    }
}