package com.teamtiger.userservice.vendors.models;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * A DTO for surface level vendor information (used for getting lots of vendors at once)
 */
@Builder
@Getter
public class BasicVendorDTO {

    private UUID vendorId;
    private String vendorName;
    private String vendorDescription;

}
