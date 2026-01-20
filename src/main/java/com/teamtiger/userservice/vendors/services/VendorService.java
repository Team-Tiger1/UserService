package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.vendors.models.*;

public interface VendorService {

    VendorRegisterDTO createVendor(CreateVendorDTO createVendorDTO);

    VendorRegisterDTO loginVendor(LoginVendorDTO loginVendorDTO);

    VendorDTO updateVendorDetails(UpdateVendorDTO updateVendorDTO, String accessToken);

    void updatePassword(UpdateVendorPasswordDTO passwordDTO, String accessToken);

    VendorDTO getVendorProfile(String accessToken);

}
