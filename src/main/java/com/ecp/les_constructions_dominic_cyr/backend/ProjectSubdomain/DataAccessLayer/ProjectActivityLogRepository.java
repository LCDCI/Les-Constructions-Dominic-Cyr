package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectActivityLogRepository extends JpaRepository<ProjectActivityLog, Long> {
    List<ProjectActivityLog> findByProjectIdentifierOrderByTimestampDesc(String projectIdentifier);
}
