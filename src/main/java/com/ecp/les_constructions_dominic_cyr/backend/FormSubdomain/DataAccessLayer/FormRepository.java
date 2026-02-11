package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Form entities.
 */
@Repository
public interface FormRepository extends JpaRepository<Form, Long> {

    /**
     * Find a form by its unique identifier
     */
    Optional<Form> findByFormIdentifier_FormId(String formId);

    /**
     * Find all forms for a specific project
     */
    List<Form> findByProjectIdentifier(String projectIdentifier);

    /**
     * Find all forms assigned to a specific customer
     */
    List<Form> findByCustomerId(String customerId);

    /**
     * Find all forms created by a specific salesperson
     */
    List<Form> findByAssignedByUserId(String assignedByUserId);

    /**
     * Find all forms of a specific type
     */
    List<Form> findByFormType(FormType formType);

    /**
     * Find all forms with a specific status
     */
    List<Form> findByFormStatus(FormStatus formStatus);

    /**
     * Find all forms for a project with a specific status
     */
    List<Form> findByProjectIdentifierAndFormStatus(String projectIdentifier, FormStatus formStatus);

    /**
     * Find all forms assigned to a customer with a specific status
     */
    List<Form> findByCustomerIdAndFormStatus(String customerId, FormStatus formStatus);

    /**
     * Find all forms for a project and customer
     */
    List<Form> findByProjectIdentifierAndCustomerId(String projectIdentifier, String customerId);

    /**
     * Find all forms by project, customer, and type
     */
    Optional<Form> findByProjectIdentifierAndCustomerIdAndFormType(
            String projectIdentifier, String customerId, FormType formType);

    /**
     * Count forms by status for a project
     */
    @Query("SELECT COUNT(f) FROM Form f WHERE f.projectIdentifier = :projectId AND f.formStatus = :status")
    Long countByProjectAndStatus(@Param("projectId") String projectId, @Param("status") FormStatus status);

    /**
     * Find all reopened forms (for notifications/alerts)
     */
    List<Form> findByFormStatusAndReopenedDateIsNotNull(FormStatus formStatus);

    /**
     * Check if a customer has a form of a specific type for a project
     */
    boolean existsByProjectIdentifierAndCustomerIdAndFormType(
            String projectIdentifier, String customerId, FormType formType);
}
