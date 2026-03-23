package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.VendorConstants;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for updating vendor information")
public class UpdateVendorDTO {
    @Schema(description = "Company name")
    private String companyName;

    @Email
    @Schema(description = "Email")
    private String email;
    @Schema(description = "Street address")
    private String streetAddress;
    @Schema(description = "Postcode")
    private String postcode;

    @Size(max = VendorConstants.PHONE_NUMBER_LENGTH)
    @Schema(description = "Phone number")
    private String phoneNumber;

    @Schema(description = "Vendor description")
    private String description;
}
