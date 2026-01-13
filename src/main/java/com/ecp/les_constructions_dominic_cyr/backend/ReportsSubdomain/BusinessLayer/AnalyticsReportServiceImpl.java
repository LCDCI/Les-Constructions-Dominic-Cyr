package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr. backend.ReportsSubdomain. DataAccessLayer.AnalyticsReportRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import java.util.List;
import java.io.ByteArrayOutputStream;

@Service
public class AnalyticsReportServiceImpl implements AnalyticsReportService {

    private final AnalyticsReportRepository reportRepository;
    private final ReportGenerationService reportGenerationService;
    private final RestTemplate restTemplate;

    private static final String FILES_SERVICE_URL = "http://localhost:8082/api/v1/files";

    public AnalyticsReportServiceImpl(AnalyticsReportRepository reportRepository,
                                      ReportGenerationService reportGenerationService,
                                      RestTemplate restTemplate) {
        this.reportRepository = reportRepository;
        this.reportGenerationService = reportGenerationService;
        this. restTemplate = restTemplate;
    }

    @Override
    public AnalyticsReport generateReport(AnalyticsReport report) {
        AnalyticsReport savedReport = reportRepository.save(report);

        new Thread(() -> {
            try {
                byte[] fileData = reportGenerationService.generateReportFile(savedReport);

                String fileIdentifier = uploadToFilesService(fileData, savedReport);

                reportRepository.updateFileIdentifier(savedReport.getReportIdentifier(), fileIdentifier);
                reportRepository.updateStatus(savedReport.getReportIdentifier(), "COMPLETED");

            } catch (Exception e) {
                reportRepository.updateStatus(savedReport.getReportIdentifier(), "FAILED");
                e.printStackTrace();
            }
        }).start();

        return savedReport;
    }

    @Override
    public AnalyticsReport getReportByIdentifier(String reportIdentifier) {
        return reportRepository.findByReportIdentifier(reportIdentifier)
                .orElseThrow(() -> new RuntimeException("Report not found with identifier:  " + reportIdentifier));
    }

    @Override
    public List<AnalyticsReport> getReportsByUser(String userId) {
        return reportRepository. findByGeneratedBy(userId);
    }

    @Override
    public List<AnalyticsReport> getAllReports() {
        return reportRepository. findAll();
    }

    @Override
    public byte[] downloadReport(String reportIdentifier) {
        AnalyticsReport report = getReportByIdentifier(reportIdentifier);

        if (report.getFileIdentifier() == null) {
            throw new RuntimeException("Report file not available");
        }

        String downloadUrl = FILES_SERVICE_URL + "/" + report.getFileIdentifier() + "/download";

        ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to download report file");
    }

    private String uploadToFilesService(byte[] fileData, AnalyticsReport report) {
        String uploadUrl = FILES_SERVICE_URL + "/upload";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        String extension = report.getFileFormat().toLowerCase();
        String filename = report. getReportName().replaceAll("\\s+", "_") + "." + extension;

        ByteArrayResource fileResource = new ByteArrayResource(fileData) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        body.add("file", fileResource);
        body.add("fileType", "REPORT");
        body.add("projectIdentifier", "SYSTEM");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return extractFileIdentifier(response.getBody());
        }

        throw new RuntimeException("Failed to upload report to files service");
    }

    private String extractFileIdentifier(String responseBody) {
        int start = responseBody.indexOf("\"fileIdentifier\":\"") + 18;
        int end = responseBody.indexOf("\"", start);
        return responseBody.substring(start, end);
    }
}