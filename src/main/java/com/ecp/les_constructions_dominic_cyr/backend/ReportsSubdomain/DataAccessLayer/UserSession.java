package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import java.time.LocalDateTime;

public class UserSession {
    private Long sessionId;
    private String sessionIdentifier;
    private String userId;
    private String visitorId;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private Integer durationSeconds;
    private Integer pagesViewed;
    private Boolean isNewVisitor;
    private String referrerSource;
    private String deviceType;
    private String browser;
    private String location;
    private LocalDateTime createdAt;

    public UserSession() {
    }

    public UserSession(String sessionIdentifier, String visitorId, LocalDateTime sessionStart,
                       Boolean isNewVisitor, String referrerSource) {
        this.sessionIdentifier = sessionIdentifier;
        this.visitorId = visitorId;
        this.sessionStart = sessionStart;
        this.isNewVisitor = isNewVisitor;
        this.referrerSource = referrerSource;
        this.pagesViewed = 0;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    public LocalDateTime getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(LocalDateTime sessionStart) {
        this.sessionStart = sessionStart;
    }

    public LocalDateTime getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(LocalDateTime sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Integer getPagesViewed() {
        return pagesViewed;
    }

    public void setPagesViewed(Integer pagesViewed) {
        this.pagesViewed = pagesViewed;
    }

    public Boolean getIsNewVisitor() {
        return isNewVisitor;
    }

    public void setIsNewVisitor(Boolean isNewVisitor) {
        this.isNewVisitor = isNewVisitor;
    }

    public String getReferrerSource() {
        return referrerSource;
    }

    public void setReferrerSource(String referrerSource) {
        this.referrerSource = referrerSource;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}