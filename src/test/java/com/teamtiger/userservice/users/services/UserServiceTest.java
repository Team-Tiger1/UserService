// set JAVA_HOME=C:\Users\ivyfi\AppData\Local\Programs\Microsoft\jdk-17.0.17.10-hotspot
// .\mvnw.cmd test

package com.teamtiger.userservice.users.services;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.teamtiger.userservice.users.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.exceptions.EmailAlreadyTakenException;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.LoginDTO;
import com.teamtiger.userservice.users.models.UserDTO;
import com.teamtiger.userservice.users.models.UserRegisterDTO;

/**
 * Unit Tests for User Services
 * {@link UserServiceJPA}
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private UsernameGenerator usernameGenerator;

    @InjectMocks
    private UserServiceJPA userService;

    private CreateUserDTO createUserDTO;
    private LoginDTO loginDTO;
    private User testUser;
    private UUID testUserId;

    /**
     * Set up required for each test
     */
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID(); // Creates a new UUID for each test

        createUserDTO = CreateUserDTO.builder()
                .email("test@exeter.ac.uk")
                .password("password123")
                .build();

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@exeter.ac.uk");
        loginDTO.setPassword("password123");

        testUser = User.builder()
                .id(testUserId)
                .username("testUsername")
                .email("test@exeter.ac.uk")
                .password("hashedPassword123")
                .build();
    }

    /**
     * Tests create new user. Should generate username, hash password, save user, and return refresh token
     */
    @Test
    void testCreateUser() {
        
        String generatedUsername = "testUsername";
        String hashedPassword = "hashedPassword123";
        String refreshToken = "refreshToken123";

        when(usernameGenerator.generateUsername()).thenReturn(generatedUsername);
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        when(passwordHasher.hashPassword(createUserDTO.getPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenUtil.generateRefreshToken(testUserId, Role.USER)).thenReturn(refreshToken);

        UserRegisterDTO result = userService.createUser(createUserDTO);

        assertNotNull(result);
        assertNotNull(result.getUserDTO());
        assertEquals(testUserId, result.getUserDTO().getId());
        assertEquals("test@exeter.ac.uk", result.getUserDTO().getEmail());
        assertEquals(generatedUsername, result.getUserDTO().getUsername());
        assertEquals(refreshToken, result.getRefreshToken());

        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(passwordHasher).hashPassword(createUserDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateRefreshToken(testUserId, Role.USER);
    }

    /**
     * Tests unsuccessful user creation when an email is taken, should not save the user or hash the password
     */
    @Test
    void testCreateUser_EmailTaken() {
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyTakenException.class, () -> {
            userService.createUser(createUserDTO);
        });

        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordHasher, never()).hashPassword(anyString());

        verify(usernameGenerator).generateUsername();
        verify(jwtTokenUtil, never()).generateRefreshToken(any(), any());


    }

    /**
     * Tests successful user login (password and email exist and match) return token, and the user details (withing DTO)
     */
    @Test
    void testUserLogin_Success() {
        String refreshToken = "refreshToken123";
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordHasher.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenUtil.generateRefreshToken(testUserId, Role.USER)).thenReturn(refreshToken);

        UserRegisterDTO result = userService.userLogin(loginDTO);

        assertNotNull(result);
        assertNotNull(result.getUserDTO());
        assertEquals(testUserId, result.getUserDTO().getId());
        assertEquals("testUsername", result.getUserDTO().getUsername());
        assertEquals("test@exeter.ac.uk", result.getUserDTO().getEmail());
        assertEquals(refreshToken, result.getRefreshToken());

        verify(userRepository).findByEmail(loginDTO.getEmail());
        verify(passwordHasher).matches(loginDTO.getPassword(), testUser.getPassword());
        verify(jwtTokenUtil).generateRefreshToken(testUserId, Role.USER);
    }

    /**
     * Tests user login with invalid email (email that doesn't exist)
     */
    @Test
    void testUserLogin_invalidEmail() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.userLogin(loginDTO);
        });

        verify(userRepository).findByEmail(loginDTO.getEmail());
        verify(passwordHasher, never()).matches(anyString(), anyString());
        verify(jwtTokenUtil, never()).generateRefreshToken(any(), any());

    }

    /**
     * Tests a user login with an invalid password that doesn't match the stored hash
     */
    @Test
    void testUserLogin_invalidPassword() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordHasher.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(PasswordIncorrectException.class, () -> {
            userService.userLogin(loginDTO);
        });

        verify(userRepository).findByEmail(loginDTO.getEmail());
        verify(passwordHasher).matches(loginDTO.getPassword(), testUser.getPassword());
        verify(jwtTokenUtil, never()).generateRefreshToken(any(), any());
    }

    /**
     * Tests returns user profile when valid access token (repository lookup)
     */
    @Test
    void testGetUserProfile() {
        String accessToken = "accessToken123";
        when(jwtTokenUtil.getUuidFromToken(accessToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserProfile(accessToken);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("test@exeter.ac.uk", result.getEmail());
        assertEquals("testUsername", result.getUsername());

        verify(jwtTokenUtil).getUuidFromToken(accessToken);
        verify(userRepository).findById(testUserId);
    }

    /**
     * Tests attempting to get an invalid users profile
     * Meaning the access token didn't contain a valid id
     */
    @Test
    void testGetUserProfile_invalidUser() {
        String accessToken = "accessToken123";
        when(jwtTokenUtil.getUuidFromToken(accessToken)).thenReturn(testUserId);
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserProfile(accessToken);
        });

        verify(jwtTokenUtil).getUuidFromToken(accessToken);
        verify(userRepository).findById(testUserId);
    }

}
