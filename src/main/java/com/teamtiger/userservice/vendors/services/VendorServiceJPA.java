package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.exceptions.CompanyNameTakenException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.models.CreateVendorDTO;
import com.teamtiger.userservice.vendors.models.LoginVendorDTO;
import com.teamtiger.userservice.vendors.models.VendorDTO;
import com.teamtiger.userservice.vendors.models.VendorRegisterDTO;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorServiceJPA implements VendorService{

    private final VendorRepository vendorRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public VendorRegisterDTO createVendor(CreateVendorDTO createVendorDTO) {

        String trimmedCompanyName = createVendorDTO.getName().trim();
        boolean isNameTaken = vendorRepository.existsByName(trimmedCompanyName);
        if(isNameTaken) {
            throw new CompanyNameTakenException();
        }

        //Hash password
        String hashedPassword = passwordHasher.hashPassword(createVendorDTO.getPassword().trim());

        //Build entity and save
        Vendor vendor = Vendor.builder()
                .name(createVendorDTO.getName())
                .email(createVendorDTO.getEmail())
                .description(createVendorDTO.getDescription())
                .streetAddress(createVendorDTO.getStreetAddress())
                .postcode(createVendorDTO.getPostcode())
                .phoneNumber(createVendorDTO.getPhoneNumber())
                .category(createVendorDTO.getCategory())
                .password(hashedPassword)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);

        //Generate a refresh token
        String refreshToken = jwtTokenUtil.generateRefreshToken(vendor.getId(), "VENDOR");

        return VendorRegisterDTO.builder()
                .vendorDTO(VendorMapper.toDTO(savedVendor))
                .refreshToken(refreshToken)
                .build();

    }


    @Override
    public VendorRegisterDTO loginVendor(LoginVendorDTO loginVendorDTO) {
        String trimmedEmail = loginVendorDTO.getEmail().trim();

        Vendor vendor = vendorRepository.findByEmail(trimmedEmail)
                .orElseThrow(CompanyNotFoundException::new);

        //Check if passwords match
        boolean isPasswordCorrect = passwordHasher.matches(loginVendorDTO.getPassword(), vendor.getPassword());

        if(!isPasswordCorrect) {
            throw new PasswordIncorrectException();
        }

        //Create new refresh token
        String refreshToken = jwtTokenUtil.generateRefreshToken(vendor.getId(), "VENDOR");

        return VendorRegisterDTO.builder()
                .refreshToken(refreshToken)
                .vendorDTO(VendorMapper.toDTO(vendor))
                .build();

    }

    private static class VendorMapper {

        public static VendorDTO toDTO(Vendor vendor) {
            return VendorDTO.builder()
                    .companyName(vendor.getName())
                    .email(vendor.getEmail())
                    .description(vendor.getDescription())
                    .streetAddress(vendor.getStreetAddress())
                    .postcode(vendor.getPostcode())
                    .phoneNumber(vendor.getPhoneNumber())
                    .category(vendor.getCategory())
                    .build();
        }

        public static Vendor toEntity(VendorDTO vendorDTO) {
            return Vendor.builder()
                    .name(vendorDTO.getCompanyName())
                    .email(vendorDTO.getEmail())
                    .description(vendorDTO.getDescription())
                    .streetAddress(vendorDTO.getStreetAddress())
                    .postcode(vendorDTO.getPostcode())
                    .phoneNumber(vendorDTO.getPhoneNumber())
                    .category(vendorDTO.getCategory())
                    .build();
        }

    }

}
