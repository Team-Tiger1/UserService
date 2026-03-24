package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.users.entities.disputes.DisputeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for updating a dispute")
public class UpdateDisputeDTO {

    @NotNull
    @Schema(description = "Dispute ID")
    private UUID disputeId;

    @NotNull
    @Schema(
            description = "Final status for the dispute",
            implementation = DisputeStatus.class
    )
    private DisputeStatus finalStatus;

    @NotEmpty
    @Length(max = 200)
    @Schema(description = "Vendor response")
    private String vendorResponse;

}
