package com.teamtiger.userservice.vendors.models;

import lombok.Builder;

import java.util.UUID;

@Builder
public class BasicVendorDTO {

    private UUID vendorId;
    private String vendorName;
    private String vendorDescription;

}
