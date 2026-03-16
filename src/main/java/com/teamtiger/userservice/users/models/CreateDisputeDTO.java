package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.entities.disputes.DisputeReason;
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
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisputeDTO {

    @NotNull
    private UUID bundleId;

    @NotNull
    @Schema(
            description = "Reason for the user to make the dispute",
            implementation = DisputeReason.class
    )
    private DisputeReason reason;

    @NotEmpty
    @Length(max = 200)
    private String description;

}
