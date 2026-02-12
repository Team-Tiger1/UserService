package com.teamtiger.userservice.users.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for updating user password endpoint
 */
@Getter
@AllArgsConstructor
public class UpdateUserPasswordDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

}
