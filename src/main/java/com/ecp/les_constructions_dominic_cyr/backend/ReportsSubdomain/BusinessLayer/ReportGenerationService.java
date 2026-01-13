package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr. backend.ReportsSubdomain. DataAccessLayer.AnalyticsReport;

public interface ReportGenerationService {
    byte[] generateReportFile(AnalyticsReport report) throws Exception;
}
