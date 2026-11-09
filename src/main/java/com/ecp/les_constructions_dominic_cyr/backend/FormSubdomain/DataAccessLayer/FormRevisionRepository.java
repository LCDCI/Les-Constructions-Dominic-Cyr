package com.ecp.les_constructions_dominic_cyr.backend.FormSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormRevisionRepository extends JpaRepository<FormRevision, Long> {
    Optional<FormRevision> findFirstByFormIdentifierAndStatusOrderByCreatedAtDesc(
            String formIdentifier, FormRevisionStatus status);
    List<FormRevision> findByFormIdentifierOrderByCreatedAtDesc(String formIdentifier);
}
