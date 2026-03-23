package com.teamtiger.userservice.users.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for the creating user endpoint
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a user")
public class CreateUserDTO {

    @Email
    @NotBlank
    @Schema(description = "User email")
    private String email;

    @NotBlank
    @Schema(description = "User password")
    private String password;

}
