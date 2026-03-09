package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.users.entities.disputes.DisputeReason;
import com.teamtiger.userservice.users.entities.disputes.DisputeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DisputeDTO {

    private UUID disputeId;
    private String bundleName;
    private DisputeReason reason;
    private DisputeStatus status;
    private String description;
    private String vendorResponse;
    private LocalDateTime createdAt;

}
