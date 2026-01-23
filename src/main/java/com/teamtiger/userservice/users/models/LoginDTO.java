package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDTO {


    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
