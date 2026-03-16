package com.teamtiger.userservice.vendors.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A DTO used for registering a vendor, returning detailed information about them and their refresh token
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VendorRegisterDTO {
    private String refreshToken;
    private VendorDTO vendorDTO;
}
