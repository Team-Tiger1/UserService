package com.teamtiger.userservice.users.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user password endpoint
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating user password")
public class UpdateUserPasswordDTO {

    @NotBlank
    @Schema(description = "Old password")
    private String oldPassword;

    @NotBlank
    @Schema(description = "New password")
    private String newPassword;

}
