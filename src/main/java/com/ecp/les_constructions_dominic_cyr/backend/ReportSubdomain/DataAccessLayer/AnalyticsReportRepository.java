package com.ecp.les_constructions_dominic_cyr.backend.ReportSubdomain.DataAccessLayer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, UUID> {
    Page<AnalyticsReport> findByOwnerIdOrderByGenerationTimestampDesc(String ownerId, Pageable pageable);
    List<AnalyticsReport> findByOwnerIdOrderByGenerationTimestampDesc(String ownerId);
}