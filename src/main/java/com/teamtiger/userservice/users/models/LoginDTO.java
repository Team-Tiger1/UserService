package com.teamtiger.userservice.users.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for the user login endpoint
 */
@Data
public class LoginDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
