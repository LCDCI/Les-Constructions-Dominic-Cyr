package com.ecp.les_constructions_dominic_cyr.backend.CommunicationSubdomain.DataAccessLayer;

/**
 * Enum for notification categories/types.
 * Represents the different types of notifications that can be sent to users.
 */
public enum NotificationCategory {
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_UPDATED,
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_COMPLETED,
    FORM_SUBMITTED,
    FORM_SIGNED,
    FORM_REJECTED,
    SCHEDULE_CREATED,
    SCHEDULE_UPDATED,
    DOCUMENT_UPLOADED,
    DOCUMENT_SHARED,
    TEAM_MEMBER_ASSIGNED,
    TEAM_MEMBER_REMOVED,
    QUOTE_SUBMITTED,
    QUOTE_APPROVED,
    QUOTE_REJECTED,
    INQUIRY_RECEIVED,
    GENERAL
}
