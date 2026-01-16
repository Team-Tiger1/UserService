package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class CreateVendorDTO {

    @NotBlank
    @Max(VendorConstants.COMPANY_NAME_MAX_LENGTH)
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String postcode;

    @NotBlank
    @Max(VendorConstants.PHONE_NUMBER_LENGTH)
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

}
