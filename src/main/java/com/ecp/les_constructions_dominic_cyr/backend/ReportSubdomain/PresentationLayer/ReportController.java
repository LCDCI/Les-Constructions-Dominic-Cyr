package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.BusinessLayer.ReportService;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.MapperLayer.ReportMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportMapper reportMapper;

    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generateReport(
            @Valid @RequestBody ReportRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {

        // FIXED: Using String for Auth0 ID instead of UUID
        String ownerId = jwt.getSubject();

        LocalDateTime startDate = request.getStartDate() != null ?
                request.getStartDate() : LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = request.getEndDate() != null ?
                request.getEndDate() : LocalDateTime.now();

        AnalyticsReport report = reportService.generateReport(
                ownerId,
                request.getReportType(),
                request.getFileFormat(),
                startDate,
                endDate
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportMapper.toDTO(report));
    }

    @GetMapping
    public ResponseEntity<Page<ReportResponseDTO>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        // FIXED: Using String for Auth0 ID instead of UUID
        String ownerId = jwt.getSubject();

        Page<AnalyticsReport> reports = reportService.getReportsByOwner(
                ownerId,
                PageRequest.of(page, size)
        );

        return ResponseEntity.ok(reports.map(reportMapper::toDTO));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponseDTO> getReport(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt) {

        // FIXED: Using String for Auth0 ID instead of UUID
        String ownerId = jwt.getSubject();
        AnalyticsReport report = reportService.getReportById(reportId, ownerId);

        return ResponseEntity.ok(reportMapper.toDTO(report));
    }

    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt) {

        // FIXED: Using String for Auth0 ID instead of UUID
        String ownerId = jwt.getSubject();
        AnalyticsReport report = reportService.getReportById(reportId, ownerId);
        byte[] content = reportService.downloadReport(reportId, ownerId);

        String contentType = report.getFileFormat().equalsIgnoreCase("PDF") ?
                "application/pdf" :
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        String fileName = String.format("analytics_report_%s.%s",
                report.getGenerationTimestamp().toString().replace(":", "-"),
                report.getFileFormat().toLowerCase());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(content);
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable UUID reportId,
            @AuthenticationPrincipal Jwt jwt) {

        String ownerId = jwt.getSubject();
        reportService.deleteReport(reportId, ownerId);

        return ResponseEntity.noContent().build();
    }
}