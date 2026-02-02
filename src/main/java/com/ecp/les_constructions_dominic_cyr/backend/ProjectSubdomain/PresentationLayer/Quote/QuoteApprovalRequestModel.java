package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Quote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteApprovalRequestModel {

    /**
     * The action to perform: "APPROVE" or "REJECT"
     */
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(APPROVE|REJECT)$", message = "Action must be either APPROVE or REJECT")
    private String action;

    /**
     * Rejection reason (required only when action is "REJECT")
     */
    private String rejectionReason;

    /**
     * Validate that rejection reason is provided when rejecting
     */
    public boolean isValid() {
        if ("REJECT".equals(action)) {
            return rejectionReason != null && !rejectionReason.isBlank();
        }
        return true;
    }
}
