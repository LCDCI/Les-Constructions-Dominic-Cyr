package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AnalyticsMetric {
    private Long metricId;
    private String metricIdentifier;
    private LocalDate metricDate;
    private Integer uniqueVisitors;
    private Integer totalSessions;
    private Integer totalPageviews;
    private Integer newVisitors;
    private Integer returningVisitors;
    private BigDecimal bounceRate;
    private BigDecimal avgSessionDuration;
    private BigDecimal pagesPerSession;
    private BigDecimal conversionRate;
    private Integer goalCompletions;
    private Integer organicTraffic;
    private Integer paidTraffic;
    private Integer socialTraffic;
    private Integer directTraffic;
    private BigDecimal avgPageLoadTime;
    private Integer errorCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AnalyticsMetric() {
    }

    public AnalyticsMetric(String metricIdentifier, LocalDate metricDate) {
        this.metricIdentifier = metricIdentifier;
        this.metricDate = metricDate;
        this.uniqueVisitors = 0;
        this.totalSessions = 0;
        this.totalPageviews = 0;
        this.newVisitors = 0;
        this. returningVisitors = 0;
        this.goalCompletions = 0;
        this.organicTraffic = 0;
        this. paidTraffic = 0;
        this.socialTraffic = 0;
        this.directTraffic = 0;
        this.errorCount = 0;
    }

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public String getMetricIdentifier() {
        return metricIdentifier;
    }

    public void setMetricIdentifier(String metricIdentifier) {
        this.metricIdentifier = metricIdentifier;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public Integer getUniqueVisitors() {
        return uniqueVisitors;
    }

    public void setUniqueVisitors(Integer uniqueVisitors) {
        this.uniqueVisitors = uniqueVisitors;
    }

    public Integer getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(Integer totalSessions) {
        this.totalSessions = totalSessions;
    }

    public Integer getTotalPageviews() {
        return totalPageviews;
    }

    public void setTotalPageviews(Integer totalPageviews) {
        this.totalPageviews = totalPageviews;
    }

    public Integer getNewVisitors() {
        return newVisitors;
    }

    public void setNewVisitors(Integer newVisitors) {
        this.newVisitors = newVisitors;
    }

    public Integer getReturningVisitors() {
        return returningVisitors;
    }

    public void setReturningVisitors(Integer returningVisitors) {
        this.returningVisitors = returningVisitors;
    }

    public BigDecimal getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(BigDecimal bounceRate) {
        this.bounceRate = bounceRate;
    }

    public BigDecimal getAvgSessionDuration() {
        return avgSessionDuration;
    }

    public void setAvgSessionDuration(BigDecimal avgSessionDuration) {
        this.avgSessionDuration = avgSessionDuration;
    }

    public BigDecimal getPagesPerSession() {
        return pagesPerSession;
    }

    public void setPagesPerSession(BigDecimal pagesPerSession) {
        this.pagesPerSession = pagesPerSession;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Integer getGoalCompletions() {
        return goalCompletions;
    }

    public void setGoalCompletions(Integer goalCompletions) {
        this.goalCompletions = goalCompletions;
    }

    public Integer getOrganicTraffic() {
        return organicTraffic;
    }

    public void setOrganicTraffic(Integer organicTraffic) {
        this.organicTraffic = organicTraffic;
    }

    public Integer getPaidTraffic() {
        return paidTraffic;
    }

    public void setPaidTraffic(Integer paidTraffic) {
        this.paidTraffic = paidTraffic;
    }

    public Integer getSocialTraffic() {
        return socialTraffic;
    }

    public void setSocialTraffic(Integer socialTraffic) {
        this.socialTraffic = socialTraffic;
    }

    public Integer getDirectTraffic() {
        return directTraffic;
    }

    public void setDirectTraffic(Integer directTraffic) {
        this.directTraffic = directTraffic;
    }

    public BigDecimal getAvgPageLoadTime() {
        return avgPageLoadTime;
    }

    public void setAvgPageLoadTime(BigDecimal avgPageLoadTime) {
        this.avgPageLoadTime = avgPageLoadTime;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
