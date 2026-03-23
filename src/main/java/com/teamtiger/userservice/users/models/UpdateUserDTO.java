package com.teamtiger.userservice.users.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating email endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for updating user email")
public class UpdateUserDTO {

    @NotBlank
    @Email
    @Schema(description = "Email")
    private String email;

}
