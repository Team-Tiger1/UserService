package com.teamtiger.userservice.vendors.models;

import lombok.Builder;
import lombok.Getter;

/**
 * A DTO used for registering a vendor, returning detailed information about them and their refresh token
 */
@Builder
@Getter
public class VendorRegisterDTO {
    private String refreshToken;
    private VendorDTO vendorDTO;
}
