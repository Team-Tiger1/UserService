package com.teamtiger.userservice.users.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
