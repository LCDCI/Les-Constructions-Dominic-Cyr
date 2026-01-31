package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot;

import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.DataAccessLayer.Lot.LotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LotResponseModel {
    private Integer id;
    @JsonProperty("lotId")
    private String lotId;
    private String lotNumber;
    private String civicAddress;
    private Float price;
    private String dimensionsSquareFeet;
    private String dimensionsSquareMeters;
    private LotStatus lotStatus;

    // Support multiple assigned users
    private List<AssignedUserInfo> assignedUsers;

    // Project information (like Schedule does)
    private Long projectId;
    private String projectIdentifier;
    private String projectName;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssignedUserInfo {
        private String userId;
        private String fullName;
        private String email;
        private String role;
    }
}
