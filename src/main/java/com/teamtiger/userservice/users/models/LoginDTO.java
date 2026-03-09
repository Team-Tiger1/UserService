package com.teamtiger.userservice.users.models;

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
public class LoginDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
