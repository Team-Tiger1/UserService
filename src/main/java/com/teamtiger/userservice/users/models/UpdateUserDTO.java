package com.teamtiger.userservice.users.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO for updating email endpoint
 */
@Data
public class UpdateUserDTO {

    @NotBlank
    @Email
    private String email;

}
