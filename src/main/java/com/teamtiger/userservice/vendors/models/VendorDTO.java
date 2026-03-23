package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.entities.VendorCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO used for getting detailed information about a vendor
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Information on vendor")
public class VendorDTO {
    @Schema(description = "Company name")
    private String companyName;
    @Schema(description = "Email")
    private String email;
    @Schema(description = "Phone number")
    private String phoneNumber;
    @Schema(description = "Street address")
    private String streetAddress;
    @Schema(description = "Postcode")
    private String postcode;
    @Schema(description = "Vendor description")
    private String description;
    @Schema(
            description = "Vendor category",
            implementation = VendorCategory.class
    )
    private VendorCategory category;


}
