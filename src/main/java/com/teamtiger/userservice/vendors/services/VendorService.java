package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.vendors.models.CreateVendorDTO;
import com.teamtiger.userservice.vendors.models.VendorRegisterDTO;

public interface VendorService {

    VendorRegisterDTO createVendor(CreateVendorDTO createVendorDTO);

}
