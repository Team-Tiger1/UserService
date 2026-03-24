package com.teamtiger.userservice.vendors.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DTO used for a vendor to login
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for vendor login")
public class LoginVendorDTO {

    @NotBlank
    @Email
    @Schema(description = "Vendor email")
    private String email;

    @NotBlank
    @Schema(description = "Vendor password")
    private String password;

}
