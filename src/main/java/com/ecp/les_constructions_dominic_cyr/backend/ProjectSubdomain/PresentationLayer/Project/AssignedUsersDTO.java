package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedUsersDTO {
    private UserSummaryDTO contractor;
    private UserSummaryDTO salesperson;
    private UserSummaryDTO customer;
}