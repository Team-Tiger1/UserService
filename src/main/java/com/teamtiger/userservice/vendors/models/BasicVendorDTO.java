package com.teamtiger.userservice.vendors.models;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class BasicVendorDTO {

    private UUID vendorId;
    private String vendorName;
    private String vendorDescription;

}
