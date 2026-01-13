package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer.AnalyticsReport;

import java.util.List;

public interface AnalyticsReportService {
    AnalyticsReport generateReport(AnalyticsReport report);
    AnalyticsReport getReportByIdentifier(String reportIdentifier);
    List<AnalyticsReport> getReportsByUser(String userId);
    List<AnalyticsReport> getAllReports();
    byte[] downloadReport(String reportIdentifier);
}
