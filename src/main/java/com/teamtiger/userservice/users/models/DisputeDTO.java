package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.entities.disputes.DisputeReason;
import com.teamtiger.userservice.users.entities.disputes.DisputeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class DisputeDTO {

    private String vendorName;

    private String bundleName;

    @Schema(
            description = "Current status of the bundle",
            implementation = DisputeStatus.class
    )
    private DisputeStatus status;

    @Schema(
            description = "Reason for the user to make the dispute",
            implementation = DisputeReason.class
    )
    private DisputeReason reason;

    private String description;


}
