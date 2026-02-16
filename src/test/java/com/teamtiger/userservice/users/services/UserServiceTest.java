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

//unit tests for UserServices
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

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID(); //make a new UUID for each test, used for testUser

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
     * createUser should generate username, hash password, save user, return refresh token
     */
    @Test
    void testCreateUser() {
        
        String generatedUsername = "testUsername";
        String hashedPassword = "hashedPassword123";
        String refreshToken = "refreshToken123";

        //creates the mock behaviour, if called with x parameters, return y
        when(usernameGenerator.generateUsername()).thenReturn(generatedUsername);
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        //as password hashing isn't known
        when(passwordHasher.hashPassword(createUserDTO.getPassword())).thenReturn(hashedPassword);

        //when any user saving is called, return the test users object
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenUtil.generateRefreshToken(testUserId, Role.USER)).thenReturn(refreshToken);

        //gets the result from the statements above
        UserRegisterDTO result = userService.createUser(createUserDTO);

        //check the values returned
        assertNotNull(result);
        assertNotNull(result.getUserDTO());
        assertEquals(testUserId, result.getUserDTO().getId());
        assertEquals("test@exeter.ac.uk", result.getUserDTO().getEmail());
        assertEquals(generatedUsername, result.getUserDTO().getUsername());
        assertEquals(refreshToken, result.getRefreshToken());

        //ensures the asserts were called on the expected dependencies
        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(passwordHasher).hashPassword(createUserDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateRefreshToken(testUserId, Role.USER);
    }

    /**
     * tests unsuccessful user creation,
     * email is taken/already exists
     * ensure it does not save the user or hash the password
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
     * tests successful user login (password and email exist and match)
     * return the DTO
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
     * tests user login with invalid email (email that doesn't exist)
     *  ensure it doesn't try to verify the password
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
     * tests a user login with an invalid password
     *  using an email that does exist
     * ensure no refresh token is made
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
     * tests getting the user from the repository, using the access token
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
     * tests attempting to get an invalid users profile
     *  meaning the access token didn't contain a valid id
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



    //not finished, this yet, will come back to when done some work on other test files
    // needs UpdateUserDTO
    // @Test
    // void testUpdateUserProfile() {
    //     String accessToken = "accessToken123";
    //     UpdateUserDTO updateDTO = new UpdateUserDTO();
    //     updateDTO.setEmail("updatesEmail@exeter.ac.uk");

    //     when(jwtTokenUtil.getUuidFromToken(accessToken)).thenReturn(testUserId);
    //     when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    //     when(userRepository.existsByEmail("updatesEmail@exeter.ac.uk")).thenReturn(false);
    //     when(userRepository.save(any(User.class))).thenReturn(testUser);



    //     UserDTO result = userService.updateUserProfile(accessToken, updateDTO);

    //     assertNotNull(result);
    //     verify(jwtTokenUtil).getUuidFromToken(accessToken);
    //     verify(userRepository).findById(testUserId);
    //     verify(userRepository).existsByEmail("updatesEmail@exeter.ac.uk");
    //     verify(userRepository, never()).save(any(User.class));
    // }
}
