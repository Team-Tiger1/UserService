package com.teamtiger.userservice.vendors;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.entities.VendorCategory;
import com.teamtiger.userservice.vendors.models.CreateVendorDTO;
import com.teamtiger.userservice.vendors.models.VendorRegisterDTO;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import com.teamtiger.userservice.vendors.services.VendorServiceJPA;

/**
 * Tests for Vendor Services {@link com.teamtiger.userservice.vendors.services.VendorServiceJPA}
 */
@ExtendWith(MockitoExtension.class)
class VendorServicesTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private VendorServiceJPA vendorService;

    private UUID testVendorId;
    private Vendor testVendor;
    private CreateVendorDTO createVendorDTO;
    private String testAccessToken;
    private String testRefreshToken;
    private String hashedPassword;

    @BeforeEach
    void setUp() {

        testVendorId = UUID.randomUUID();
//        testAccessToken = "test_AccToken";
        testRefreshToken = "test_RefToken";
        hashedPassword = "hashedPassword";

        //what the mock repo returns
        testVendor = Vendor.builder()
                .id(testVendorId)
                .name("Test Restaurant")
                .email("test@exeter.ac.uk")
                .phoneNumber("0123456789")
                .streetAddress("forum")
                .postcode("EX4 ABC")
                .description("A test restaurant")
                .password(hashedPassword)
                .category(VendorCategory.RESTAURANT)
                .build();

        //what controller receives (raw input)
        createVendorDTO = CreateVendorDTO.builder()
                .name("Test Restaurant")
                .email("test@exeter.ac.uk")
                .phoneNumber("0123456789")
                .streetAddress("streatham court")
                .postcode("EX4 4XE")
                .description("A test restaurant")
                .password("Password123")
                .category(VendorCategory.RESTAURANT)
                .build();
    }


    /**
     * Test creating a vendor successfully
     * check name uniqueness, password hashing happens, vendor is saved, refresh token given
     */
    @Test
    void testCreateVendor_Success() {

        //add behaviour to mock objects
        when(vendorRepository.existsByName("Test Restaurant")).thenReturn(false);
        when(passwordHasher.hashPassword("Password123")).thenReturn(hashedPassword);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(testVendor);
        when(jwtTokenUtil.generateRefreshToken(nullable(UUID.class), eq(Role.VENDOR))).thenReturn(testRefreshToken);

        VendorRegisterDTO result = vendorService.createVendor(createVendorDTO);


        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken()).isEqualTo(testRefreshToken);
        assertThat(result.getVendorDTO()).isNotNull();
        assertThat(result.getVendorDTO().getCompanyName()).isEqualTo("Test Restaurant");
        assertThat(result.getVendorDTO().getEmail()).isEqualTo("test@exeter.ac.uk");
        assertThat(result.getVendorDTO().getCategory()).isEqualTo(VendorCategory.RESTAURANT);

        verify(vendorRepository).existsByName("Test Restaurant");
        verify(passwordHasher).hashPassword("Password123");
        verify(vendorRepository).save(any(Vendor.class));
        verify(jwtTokenUtil).generateRefreshToken(nullable(UUID.class), eq(Role.VENDOR));
    }

    /**
     * Trimmed restaurant names should be saved without leading or trailing whitespace
     */
    @Test
    void testCreateVendor_Trim() {


        CreateVendorDTO testTrimCreateVendorDTO = CreateVendorDTO.builder()
                .name("  Trim Test Restaurant  ")
                .email("test@restaurant.com")
                .phoneNumber("0123456789")
                .streetAddress("forum")
                .postcode("EX4 BCD")
                .password("Password123")
                .category(VendorCategory.RESTAURANT)
                .build();

        when(vendorRepository.existsByName("Trim Test Restaurant")).thenReturn(false);
        when(passwordHasher.hashPassword("Password123")).thenReturn(hashedPassword);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(testVendor);
        when(jwtTokenUtil.generateRefreshToken(nullable(UUID.class), eq(Role.VENDOR))).thenReturn(testRefreshToken);

        vendorService.createVendor(testTrimCreateVendorDTO);

        verify(vendorRepository).existsByName("Trim Test Restaurant");
    }
}