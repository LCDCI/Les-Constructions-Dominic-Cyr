package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project;

/**
 * Enum for project activity types.
 * Ensures type-safe and consistent activity logging across the application.
 */
public enum ActivityType {
    CONTRACTOR_ASSIGNED,
    CONTRACTOR_REMOVED,
    SALESPERSON_ASSIGNED,
    SALESPERSON_REMOVED,
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_COMPLETED
}
