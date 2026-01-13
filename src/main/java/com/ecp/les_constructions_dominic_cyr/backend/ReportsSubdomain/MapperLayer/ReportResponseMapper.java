package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr. backend.ReportsSubdomain. DataAccessLayer.AnalyticsReport;
import com.ecp. les_constructions_dominic_cyr.backend.ReportsSubdomain.PresentationLayer. ReportResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportResponseMapper {

    @Value("${files.service.base-url: http://localhost:8082}")
    private String filesServiceBaseUrl;

    public ReportResponseDTO toDTO(AnalyticsReport report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setReportIdentifier(report.getReportIdentifier());
        dto.setReportName(report. getReportName());
        dto.setReportType(report. getReportType());
        dto.setFileFormat(report.getFileFormat());
        dto.setStartDate(report.getStartDate());
        dto.setEndDate(report.getEndDate());
        dto.setGeneratedBy(report.getGeneratedBy());
        dto.setGeneratedAt(report.getGeneratedAt());
        dto.setFileIdentifier(report.getFileIdentifier());
        dto.setStatus(report.getStatus());

        if (report.getFileIdentifier() != null && filesServiceBaseUrl != null) {
            dto.setDownloadUrl(filesServiceBaseUrl + "/files/" + report.getFileIdentifier() + "/download");
        }

        return dto;
    }
}