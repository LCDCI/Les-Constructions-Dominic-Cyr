package com.ecp.les_constructions_dominic_cyr.backend.ReportsSubdomain.DataAccessLayer;

import java.time.LocalDateTime;

public class PageView {
    private Long pageViewId;
    private String pageViewIdentifier;
    private String sessionIdentifier;
    private String pageUrl;
    private String pageTitle;
    private LocalDateTime viewTimestamp;
    private Integer timeOnPageSeconds;
    private Boolean isExit;
    private Boolean isBounce;
    private LocalDateTime createdAt;

    public PageView() {
    }

    public PageView(String pageViewIdentifier, String sessionIdentifier, String pageUrl,
                    String pageTitle, LocalDateTime viewTimestamp) {
        this.pageViewIdentifier = pageViewIdentifier;
        this.sessionIdentifier = sessionIdentifier;
        this.pageUrl = pageUrl;
        this.pageTitle = pageTitle;
        this.viewTimestamp = viewTimestamp;
        this.isExit = false;
        this.isBounce = false;
    }

    public Long getPageViewId() {
        return pageViewId;
    }

    public void setPageViewId(Long pageViewId) {
        this.pageViewId = pageViewId;
    }

    public String getPageViewIdentifier() {
        return pageViewIdentifier;
    }

    public void setPageViewIdentifier(String pageViewIdentifier) {
        this.pageViewIdentifier = pageViewIdentifier;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public LocalDateTime getViewTimestamp() {
        return viewTimestamp;
    }

    public void setViewTimestamp(LocalDateTime viewTimestamp) {
        this.viewTimestamp = viewTimestamp;
    }

    public Integer getTimeOnPageSeconds() {
        return timeOnPageSeconds;
    }

    public void setTimeOnPageSeconds(Integer timeOnPageSeconds) {
        this.timeOnPageSeconds = timeOnPageSeconds;
    }

    public Boolean getIsExit() {
        return isExit;
    }

    public void setIsExit(Boolean isExit) {
        this.isExit = isExit;
    }

    public Boolean getIsBounce() {
        return isBounce;
    }

    public void setIsBounce(Boolean isBounce) {
        this.isBounce = isBounce;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
