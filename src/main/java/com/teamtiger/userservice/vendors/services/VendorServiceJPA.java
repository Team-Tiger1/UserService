package com.teamtiger.userservice.vendors.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.entities.disputes.Dispute;
import com.teamtiger.userservice.users.entities.disputes.DisputeReason;
import com.teamtiger.userservice.users.exceptions.AuthorizationException;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.repositories.DisputeRepository;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.exceptions.CompanyNameTakenException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.exceptions.DisputeNotFoundException;
import com.teamtiger.userservice.vendors.models.*;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorServiceJPA implements VendorService{

    private final VendorRepository vendorRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Creates a new vendor and stores the record on the database
     * @param createVendorDTO A valid request body with the information for the vendor account
     * @return A VendorRegisterDTO that has the vendor information and refresh token
     */
    @Transactional
    @Override
    public VendorRegisterDTO createVendor(CreateVendorDTO createVendorDTO) {

        //Format username and check if taken
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

    /**
     * Allows a vendor to login, and checks a vendor details against the database
     * @param loginVendorDTO A valid login request body
     * @return A VendorRegisterDTO that has the vendor record and refresh token
     */
    @Override
    public VendorRegisterDTO loginVendor(LoginVendorDTO loginVendorDTO) {

        //Format email and query database
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

    /**
     * Updates a vendors database record with the details provided
     * @param accessToken An access token (has vendorId in the payload)
     * @param updateVendorDTO Has the details that are being updated
     * @return The new vendor details after they've been updated
     */
    @Transactional
    @Override
    public VendorDTO updateVendorDetails(UpdateVendorDTO updateVendorDTO, String accessToken) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Get vendorId and query database
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Vendor savedVendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        //Update all details that aren't null

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

    /**
     * Updates a vendor's password, given that their old one is correct
     * @param accessToken An access token (has vendorId in the payload)
     * @param passwordDTO The new password and old password
     */
    @Transactional
    @Override
    public void updatePassword(UpdateVendorPasswordDTO passwordDTO, String accessToken) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Extract vendorId and query database
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        //Check if plain text password matches stored one
        boolean doesOldPasswordMatch = passwordHasher.matches(passwordDTO.getOldPassword(), vendor.getPassword());
        if(!doesOldPasswordMatch) {
            throw new PasswordIncorrectException();
        }

        String hashedPassword = passwordHasher.hashPassword(passwordDTO.getNewPassword());
        vendor.setPassword(hashedPassword);

        vendorRepository.save(vendor);

    }

    /**
     * Gets the vendor's details from the database
     * @param accessToken An access token (has vendorId in the payload)
     * @return vendor details from the database
     */
    @Override
    public VendorDTO getVendorProfile(String accessToken) {

        //Check if role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Extract vendorId and query database
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Vendor savedVendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        return VendorMapper.toDTO(savedVendor);
    }

    /**
     * Saves seeded vendors to the database
     * @param accessToken An access token (has vendorId in the payload)
     * @param vendors List of generated vendors
     */
    @Transactional
    @Override
    public void loadSeededData(String accessToken, List<VendorSeedDTO> vendors) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        //Convert request data to vendor entities
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

    /**
     * Gets all vendors from the database
     * @return A list of vendors
     */
    @Cacheable(value = "vendors")
    @Override
    public List<BasicVendorDTO> getAllVendors() {

        List<Vendor> vendorList = vendorRepository.findAll();

        //Only extract the basic data and convert to DTOs
        return vendorList.stream()
                .map(entity -> BasicVendorDTO.builder()
                        .vendorId(entity.getId())
                        .vendorName(entity.getName())
                        .vendorDescription(entity.getDescription())
                        .build())
                .toList();

    }

    /**
     * Get detailed vendor information from the database
     * @param vendorId The vendors UUID
     */
    @Override
    @Cacheable(value = "vendor_info", key = "#vendorId")
    public VendorDTO getDetailedVendorInfo(UUID vendorId) {

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(CompanyNotFoundException::new);

        return VendorMapper.toDTO(vendor);
    }

    /**
     * Gets all disputes related with a vendor
     * @param accessToken The vendors access token
     * @return A list of disputes
     */
    @Cacheable(value = "user_disputes", key = "@jwtTokenUtil.getUuidFromToken(#accessToken)")
    @Override
    public List<DisputeDTO> getAllDisputes(String accessToken) {

        //Check if role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Extract vendorId and query database
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Set<Dispute> savedDisputes = disputeRepository.findAllDisputesByVendor(vendorId);

        return savedDisputes.stream()
                .map(entity -> DisputeDTO.builder()
                        .disputeId(entity.getId())
                        .bundleName(disputeRepository.findBundleName(entity.getBundleId()))
                        .reason(entity.getReason())
                        .description(entity.getDescription())
                        .vendorResponse(entity.getVendorResponse())
                        .status(entity.getStatus())
                        .createdAt(entity.getTimeCreated())
                        .build()
                ).toList();
    }


    /**
     * Validates the vendor, updates the dispute and saves it
     * @param accessToken The vendors access token
     * @param updateDisputeDTO The new information for the dispute
     * @return The updated dispute
     */
    @Transactional
    @CacheEvict(value = "user_disputes", key = "@jwtTokenUtil.getUuidFromToken(#accessToken)")
    @Override
    public DisputeDTO updateDispute(String accessToken, UpdateDisputeDTO updateDisputeDTO) {

        //Check if role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Extract dispute information and query database
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        Dispute savedDispute = disputeRepository.findById(updateDisputeDTO.getDisputeId())
                .orElseThrow(DisputeNotFoundException::new);

        //Check the target vendor is updating the dispute
        if(!savedDispute.getVendorId().equals(vendorId)) {
            throw new AuthorizationException();
        }

        //Update and save
        savedDispute.setStatus(updateDisputeDTO.getFinalStatus());
        savedDispute.setVendorResponse(updateDisputeDTO.getVendorResponse());

        disputeRepository.save(savedDispute);

        return DisputeDTO.builder()
                .disputeId(savedDispute.getId())
                .reason(savedDispute.getReason())
                .status(savedDispute.getStatus())
                .bundleName(disputeRepository.findBundleName(savedDispute.getBundleId()))
                .description(savedDispute.getDescription())
                .vendorResponse(savedDispute.getVendorResponse())
                .createdAt(savedDispute.getTimeCreated())
                .build();
    }


    /**
     * Deletes vendor records and vendor product records
     * @param accessToken Vendors access token
     */
    @Override
    @Transactional
    public void deleteVendor(String accessToken) {

        //Check if role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("VENDOR")) {
            throw new AuthorizationException();
        }

        //Extract id
        UUID vendorId = jwtTokenUtil.getUuidFromToken(accessToken);

        //Delete vendor and vendor products
        vendorRepository.deleteAllVendorProducts(vendorId);

        vendorRepository.deleteById(vendorId);
    }

    /**
     * Maps database entities to DTOs
     */
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

    }

}
