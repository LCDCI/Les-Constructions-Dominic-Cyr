package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

/**
 * Enum representing the status of a customer form.
 */
public enum FormStatus {
    /**
     * Form has been created but not yet assigned to a customer
     */
    DRAFT,
    
    /**
     * Form has been assigned to a customer but not yet started
     */
    ASSIGNED,
    
    /**
     * Customer is currently working on the form
     */
    IN_PROGRESS,
    
    /**
     * Customer has submitted the form
     */
    SUBMITTED,
    
    /**
     * Owner/salesperson has reopened the form for customer to re-edit
     */
    REOPENED,
    
    /**
     * Form has been completed and approved
     */
    COMPLETED
}
