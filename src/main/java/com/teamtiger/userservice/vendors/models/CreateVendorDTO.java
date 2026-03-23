package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import com.teamtiger.userservice.vendors.entities.VendorCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A DTO used for creating a new vendor
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request body for creating a vendor")
public class CreateVendorDTO {

    @NotBlank
    @Size(max = VendorConstants.COMPANY_NAME_MAX_LENGTH)
    @Schema(description = "Company name")
    private String name;

    @NotBlank
    @Schema(description = "Street address")
    private String streetAddress;

    @NotBlank
    @Schema(description = "Postcode")
    private String postcode;

    @Schema(description = "Vendor description")
    private String description; //Optional

    @NotBlank
    @Size(max = VendorConstants.PHONE_NUMBER_LENGTH)
    @Schema(description = "Phone number")
    private String phoneNumber;

    @NotBlank
    @Email
    @Schema(description = "Email")
    private String email;

    @NotNull
    @Schema(
            description = "Vendor category",
            implementation = VendorCategory.class
    )
    private VendorCategory category;

    @NotBlank
    @Schema(description = "Password")
    private String password;

}
