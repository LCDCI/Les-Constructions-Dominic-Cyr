package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Project.ActivityType;

@Entity
@Table(name = "project_activity_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_identifier")
    private String projectIdentifier;

    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    private ActivityType activityType; // CONTRACTOR_ASSIGNED, CONTRACTOR_REMOVED, SALESPERSON_ASSIGNED, SALESPERSON_REMOVED

    @Column(name = "user_identifier")
    private String userIdentifier; // The contractor or salesperson being assigned

    @Column(name = "user_name")
    private String userName;

    @Column(name = "changed_by")
    private String changedBy; // The owner/user who made the change

    @Column(name = "changed_by_name")
    private String changedByName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "description")
    private String description;
}
