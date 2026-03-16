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
public class UpdateDisputeDTO {

    @NotNull
    private UUID disputeId;

    @NotNull
    @Schema(implementation = DisputeStatus.class)
    private DisputeStatus finalStatus;

    @NotEmpty
    @Length(max = 200)
    private String vendorResponse;

}
