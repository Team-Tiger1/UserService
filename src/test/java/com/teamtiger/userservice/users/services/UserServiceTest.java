// set JAVA_HOME=C:\Users\ivyfi\AppData\Local\Programs\Microsoft\jdk-17.0.17.10-hotspot
// .\mvnw.cmd test

package com.teamtiger.userservice.users.services;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.teamtiger.userservice.users.repositories.UserRepository;




// package com.teamtiger.userservice.services;

// import com.teamtiger.userservice.users.models.CreateUserDTO;
// import com.teamtiger.userservice.users.models.UserRegisterDTO;
// import com.teamtiger.userservice.users.services.UserService;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;

// import com.teamtiger.userservice.users.exceptions.*;
// import static org.junit.jupiter.api.Assertions.*;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;

// import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.any;

// import com.teamtiger.userservice.auth.JwtTokenUtil;
// import com.teamtiger.userservice.auth.PasswordHasher;
// import com.teamtiger.userservice.auth.models.Role;

// import com.teamtiger.userservice.users.entities.User;
// import com.teamtiger.userservice.users.repositories.UserRepository;

// import org.junit.jupiter.api.BeforeEach;
// import org.mockito.junit.MockitoExtension;

// import java.util.Optional;
// import java.util.UUID;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

//unit tests for UserServicesJPA
@ExtendWith(MockitoExtension.class)
class UserServiceJPATest {

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

    @Test
    void testCreateUser() {
        
        String generatedUsername = "testUsername";
        String hashedPassword = "hashedPassword123";
        String refreshToken = "refreshToken123";
        //arrange 
        //creates the mock behaviour, like if its called with x parameters, return this
        when(usernameGenerator.generateUsername()).thenReturn(generatedUsername);
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        //as password hashing isnt known
        when(passwordHasher.hashPassword(createUserDTO.getPassword())).thenReturn(hashedPassword);
        //when any iser saving is called, return the test users object
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenUtil.generateRefreshToken(testUserId, Role.USER)).thenReturn(refreshToken);

        //act
        //calls generateUsername, existsByEmail etc etc and gets the result from the when statements above
        UserRegisterDTO result = userService.createUser(createUserDTO);

        //assert
        //check the values returned
        assertNotNull(result);
        assertNotNull(result.getUserDTO());
        assertEquals(testUserId, result.getUserDTO().getId());
        assertEquals("test@exeter.ac.uk", result.getUserDTO().getEmail());
        assertEquals(generatedUsername, result.getUserDTO().getUsername());
        assertEquals(refreshToken, result.getRefreshToken());
        //ensure the asserts were called on the correct parameters
        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(passwordHasher).hashPassword(createUserDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateRefreshToken(testUserId, Role.USER);
    }

    @Test
    void testCreateUser_EmailTaken() {
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyTakenException.class, () -> {
            userService.createUser(createUserDTO);
        });

        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUserLogin() {
        String refreshToken = "refreshToken123";
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordHasher.matches(loginDTO.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtTokenUtil.generateRefreshToken(testUserId, Role.USER)).thenReturn(refreshToken);

        UserRegisterDTO result = userService.userLogin(loginDTO);

        assertNotNull(result);
        assertNotNull(result.getUserDTO());
        assertEquals(testUserId, result.getUserDTO().getId());
        assertEquals("test@exeter.ac.uk", result.getUserDTO().getEmail());
        assertEquals(refreshToken, result.getRefreshToken());

        verify(userRepository).findByEmail(loginDTO.getEmail());
        verify(passwordHasher).matches(loginDTO.getPassword(), testUser.getPassword());
        verify(jwtTokenUtil).generateRefreshToken(testUserId, Role.USER);
    }

    @Test
    void testUserLogin_invalidEmail() {
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.userLogin(loginDTO);
        });

        verify(userRepository).findByEmail(loginDTO.getEmail());
        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

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

    @Test
    void testGetUserProfile_invlidUser() {
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
