package com.teamtiger.userservice.users.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the user login endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user login")
public class LoginDTO {

    @Email
    @NotBlank
    @Schema(description = "User email")
    private String email;

    @NotBlank
    @Schema(description = "User password")
    private String password;
}
