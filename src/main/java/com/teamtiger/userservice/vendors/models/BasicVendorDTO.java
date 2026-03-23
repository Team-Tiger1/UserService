package com.teamtiger.userservice.vendors.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A DTO for surface level vendor information (used for getting lots of vendors at once)
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BasicVendorDTO {

    private UUID vendorId;
    private String vendorName;
    private String postcode;
    private String vendorDescription;

}
