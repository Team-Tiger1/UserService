package com.teamtiger.userservice.users.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for the creating user endpoint
 */
@Getter
@Builder
@AllArgsConstructor
public class CreateUserDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

}
