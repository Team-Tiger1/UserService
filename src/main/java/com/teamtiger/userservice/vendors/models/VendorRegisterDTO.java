package com.teamtiger.userservice.vendors.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VendorRegisterDTO {
    private String refreshToken;
    private VendorDTO vendorDTO;
}
