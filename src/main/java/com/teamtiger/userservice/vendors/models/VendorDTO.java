package com.teamtiger.userservice.vendors.models;

import com.teamtiger.userservice.vendors.entities.VendorCategory;
import lombok.Builder;
import lombok.Data;

/**
 * A DTO used for getting detailed information about a vendor
 */
@Data
@Builder
public class VendorDTO {

    private String companyName;
    private String email;
    private String phoneNumber;
    private String streetAddress;
    private String postcode;
    private String description;
    private VendorCategory category;


}
