package com.teamtiger.userservice.vendors.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A DTO for a vendor to update their password
 */
@Getter
@AllArgsConstructor
public class UpdateVendorPasswordDTO {

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

}
