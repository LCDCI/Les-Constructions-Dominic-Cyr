package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignedUsersDTO {
    private List<UserSummaryDTO> contractors;
    private List<UserSummaryDTO> salespersons;
    private UserSummaryDTO customer;
}