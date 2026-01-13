package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer.AnalyticsReport;
import com. ecp.les_constructions_dominic_cyr.backend. ReportsSubdomain.PresentationLayer.ReportRequestDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReportMapper {

    public AnalyticsReport toEntity(ReportRequestDTO dto, String generatedBy) {
        String identifier = UUID.randomUUID().toString();
        return new AnalyticsReport(
                identifier,
                dto.getReportName(),
                dto.getReportType(),
                dto.getFileFormat(),
                dto.getStartDate(),
                dto.getEndDate(),
                generatedBy
        );
    }
}
