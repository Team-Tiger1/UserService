package com.teamtiger.userservice.vendors.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO used for a vendor to login
 */
@Getter
@AllArgsConstructor
public class LoginVendorDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

}
