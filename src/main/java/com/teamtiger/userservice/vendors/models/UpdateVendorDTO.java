package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * A DTO for updating vendor details (all fields are optional)
 */
@Data
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
