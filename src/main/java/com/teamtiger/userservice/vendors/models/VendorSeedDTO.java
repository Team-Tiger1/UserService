package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import com.teamtiger.userservice.vendors.entities.VendorCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
public class VendorSeedDTO {

    @NotNull
    private UUID vendorId;

    @NotBlank
    @Size(max = VendorConstants.COMPANY_NAME_MAX_LENGTH)
    private String name;

    @NotBlank
    private String streetAddress;

    @NotBlank
    private String postcode;

    private String description; //Optional

    @NotBlank
    @Size(max = VendorConstants.PHONE_NUMBER_LENGTH)
    private String phoneNumber;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private VendorCategory category;

    @NotBlank
    private String password;

}
