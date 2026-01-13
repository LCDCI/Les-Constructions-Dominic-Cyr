package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.BusinessLayer. AnalyticsReportService;
import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp. les_constructions_dominic_cyr.backend.ReportsSubdomain.MapperLayer.ReportMapper;
import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.MapperLayer.ReportResponseMapper;
import org.springframework. core.io.ByteArrayResource;
import org. springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework. http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    private final AnalyticsReportService reportService;
    private final ReportMapper reportMapper;
    private final ReportResponseMapper responseMapper;

    public ReportsController(AnalyticsReportService reportService,
                             ReportMapper reportMapper,
                             ReportResponseMapper responseMapper) {
        this.reportService = reportService;
        this.reportMapper = reportMapper;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generateReport(
            @RequestBody ReportRequestDTO requestDTO,
            Authentication authentication) {

        String userId = authentication.getName();

        AnalyticsReport report = reportMapper.toEntity(requestDTO, userId);
        AnalyticsReport savedReport = reportService.generateReport(report);
        ReportResponseDTO responseDTO = responseMapper.toDTO(savedReport);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping("/{reportIdentifier}")
    public ResponseEntity<ReportResponseDTO> getReportById(
            @PathVariable String reportIdentifier,
            Authentication authentication) {

        AnalyticsReport report = reportService.getReportByIdentifier(reportIdentifier);

        if (! report.getGeneratedBy().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReportResponseDTO responseDTO = responseMapper. toDTO(report);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<ReportResponseDTO>> getMyReports(Authentication authentication) {
        String userId = authentication.getName();

        List<AnalyticsReport> reports = reportService.getReportsByUser(userId);
        List<ReportResponseDTO> responseDTOs = reports.stream()
                .map(responseMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity. ok(responseDTOs);
    }

    @GetMapping("/{reportIdentifier}/download")
    public ResponseEntity<Resource> downloadReport(
            @PathVariable String reportIdentifier,
            Authentication authentication) {

        AnalyticsReport report = reportService. getReportByIdentifier(reportIdentifier);

        if (!report.getGeneratedBy().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (! report.getStatus().equals("COMPLETED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        byte[] fileData = reportService.downloadReport(reportIdentifier);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        String contentType = report.getFileFormat().equals("PDF")
                ? "application/pdf"
                :  "application/vnd.ms-excel";

        String extension = report.getFileFormat().toLowerCase();
        String filename = report.getReportName().replaceAll("\\s+", "_") + "_" +
                reportIdentifier. substring(0, 8) + "." + extension;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportResponseDTO>> getAllReports(Authentication authentication) {
        List<AnalyticsReport> reports = reportService.getAllReports();
        List<ReportResponseDTO> responseDTOs = reports. stream()
                .map(responseMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity. ok(responseDTOs);
    }
}
