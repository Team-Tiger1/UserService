package com.teamtiger.userservice.vendors.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DTO for a vendor to update their password
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for updating vendor password")
public class UpdateVendorPasswordDTO {

    @NotBlank
    @Schema(description = "Old password")
    private String oldPassword;

    @NotBlank
    @Schema(description = "New password")
    private String newPassword;

}
