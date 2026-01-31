package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.exceptions.AuthorizationException;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.exceptions.CompanyNameTakenException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.models.*;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
        String refreshToken = jwtTokenUtil.generateRefreshToken(vendor.getId(), Role.VENDOR);

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
        String refreshToken = jwtTokenUtil.generateRefreshToken(vendor.getId(), Role.VENDOR);

        return VendorRegisterDTO.builder()
                .refreshToken(refreshToken)
                .vendorDTO(VendorMapper.toDTO(vendor))
                .build();

    }


    @Override
    public VendorDTO updateVendorDetails(UpdateVendorDTO updateVendorDTO, String accessToken) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Vendor savedVendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        if(updateVendorDTO.getCompanyName() != null) {
            savedVendor.setName(updateVendorDTO.getCompanyName());
        }

        if(updateVendorDTO.getDescription() != null) {
            savedVendor.setDescription(updateVendorDTO.getDescription());
        }

        if(updateVendorDTO.getEmail() != null) {
            savedVendor.setEmail(updateVendorDTO.getEmail());
        }

        if(updateVendorDTO.getPhoneNumber() != null) {
            savedVendor.setPhoneNumber(updateVendorDTO.getPhoneNumber());
        }

        if(updateVendorDTO.getStreetAddress() != null) {
            savedVendor.setStreetAddress(updateVendorDTO.getStreetAddress());
        }

        if(updateVendorDTO.getPostcode() != null) {
            savedVendor.setPostcode(updateVendorDTO.getPostcode());
        }

        Vendor updatedVendor = vendorRepository.save(savedVendor);

        return VendorMapper.toDTO(updatedVendor);
    }


    @Override
    public void updatePassword(UpdateVendorPasswordDTO passwordDTO, String accessToken) {

        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        boolean doesOldPasswordMatch = passwordHasher.matches(passwordDTO.getOldPassword(), vendor.getPassword());
        if(!doesOldPasswordMatch) {
            throw new PasswordIncorrectException();
        }

        String hashedPassword = passwordHasher.hashPassword(passwordDTO.getNewPassword());
        vendor.setPassword(hashedPassword);

        vendorRepository.save(vendor);

    }

    @Override
    public VendorDTO getVendorProfile(String accessToken) {
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Vendor savedVendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        return VendorMapper.toDTO(savedVendor);
    }

    @Override
    public void loadSeededData(String accessToken, List<VendorSeedDTO> vendors) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        List<Vendor> vendorEntities = vendors.stream()
                .map(dto -> Vendor.builder()
                        .id(dto.getVendorId())
                        .name(dto.getName())
                        .email(dto.getEmail())
                        .description(dto.getDescription())
                        .streetAddress(dto.getStreetAddress())
                        .postcode(dto.getPostcode())
                        .phoneNumber(dto.getPhoneNumber())
                        .category(dto.getCategory())
                        .password(passwordHasher.hashPassword(dto.getPassword()))
                        .build())
                .toList();

        vendorRepository.saveAll(vendorEntities);


    }

    @Override
    public List<BasicVendorDTO> getAllVendors() {

        List<Vendor> vendorList = vendorRepository.findAll();

        return vendorList.stream()
                .map(entity -> BasicVendorDTO.builder()
                        .vendorId(entity.getId())
                        .vendorName(entity.getName())
                        .vendorDescription(entity.getDescription())
                        .build())
                .toList();

    }

    @Override
    public VendorDTO getDetailedVendorInfo(UUID vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        return VendorMapper.toDTO(vendor);
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
