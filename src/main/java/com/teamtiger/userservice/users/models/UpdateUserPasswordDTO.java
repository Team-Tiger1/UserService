package com.teamtiger.userservice.users.models;

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
public class UpdateUserPasswordDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

}
