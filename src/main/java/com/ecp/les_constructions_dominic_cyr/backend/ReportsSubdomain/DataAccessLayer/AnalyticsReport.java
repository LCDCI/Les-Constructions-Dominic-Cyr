package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnalyticsReport {
    private Long reportId;
    private String reportIdentifier;
    private String reportName;
    private String reportType;
    private String fileFormat;
    private LocalDate startDate;
    private LocalDate endDate;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private String filePath;
    private String fileIdentifier;
    private String status;
    private LocalDateTime createdAt;

    public AnalyticsReport() {
    }

    public AnalyticsReport(String reportIdentifier, String reportName, String reportType,
                           String fileFormat, LocalDate startDate, LocalDate endDate,
                           String generatedBy) {
        this.reportIdentifier = reportIdentifier;
        this.reportName = reportName;
        this. reportType = reportType;
        this.fileFormat = fileFormat;
        this.startDate = startDate;
        this.endDate = endDate;
        this. generatedBy = generatedBy;
        this.generatedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getReportIdentifier() {
        return reportIdentifier;
    }

    public void setReportIdentifier(String reportIdentifier) {
        this.reportIdentifier = reportIdentifier;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public void setFileIdentifier(String fileIdentifier) {
        this.fileIdentifier = fileIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
