package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer.AnalyticsReport;
import com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.PresentationLayer.ReportResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    @Value("${files.service.base-url}")
    private String filesServiceBaseUrl;

    public ReportResponseDTO toDTO(AnalyticsReport report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setReportType(report.getReportType());
        dto.setFileFormat(report.getFileFormat());
        dto.setDownloadUrl(filesServiceBaseUrl + "/download/" + report.getFileKey());
        dto.setFileSize(report.getFileSize());
        dto.setGenerationTimestamp(report.getGenerationTimestamp());
        dto.setStartDate(report.getStartDate());
        dto.setEndDate(report.getEndDate());
        dto.setStatus(report.getStatus());
        dto.setMetadata(report.getMetadata());
        return dto;
    }
}
