package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateVendorDTO {

    private String companyName;

    @Email
    private String email;

    private String streetAddress;
    private String postcode;

    @Size(max = VendorConstants.PHONE_NUMBER_LENGTH)
    private String phoneNumber;

    private String description;
}
