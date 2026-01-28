package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.vendors.models.*;

import java.util.List;
import java.util.UUID;

public interface VendorService {

    VendorRegisterDTO createVendor(CreateVendorDTO createVendorDTO);

    VendorRegisterDTO loginVendor(LoginVendorDTO loginVendorDTO);

    VendorDTO updateVendorDetails(UpdateVendorDTO updateVendorDTO, String accessToken);

    void updatePassword(UpdateVendorPasswordDTO passwordDTO, String accessToken);

    VendorDTO getVendorProfile(String accessToken);

    void loadSeededData(String accessToken, List<CreateVendorDTO> vendors);

    List<BasicVendorDTO> getAllVendors();

    VendorDTO getDetailedVendorInfo(UUID vendorId);

}
