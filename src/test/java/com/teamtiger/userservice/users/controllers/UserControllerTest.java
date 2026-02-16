// //.\mvnw.cmd test -Dtest=UserControllerTest

package com.teamtiger.userservice.users.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import com.teamtiger.userservice.users.services.UserService;
import lombok.extern.apachecommons.CommonsLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtiger.userservice.users.exceptions.EmailAlreadyTakenException;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.exceptions.UsernameAlreadyTakenException;
import com.teamtiger.userservice.users.models.*;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@CommonsLog
@WebMvcTest(UserController.class)
public class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;


    private CreateUserDTO createUserDTO;
    private LoginDTO loginDTO;
    // private User testUser;
    private UUID testUserId;
    private UserDTO userDTO;
    private UserRegisterDTO userRegisterDTO;
    private UpdateUserDTO updateUserDTO;
    private UpdateUserPasswordDTO updateUserPasswordDTO;


    @BeforeEach
    void setUp(){
        testUserId = UUID.randomUUID(); //make a new UUID for each test
        
        createUserDTO = CreateUserDTO.builder()
                .email("test@exeter.ac.uk")
                .password("password123")
                .build();


        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@exeter.ac.uk");
        loginDTO.setPassword("password123");

    
        userDTO = UserDTO.builder()     
                .id(testUserId)
                .username("testUsername")
                .email("test@exeter.ac.uk")
                .build();

        userRegisterDTO = UserRegisterDTO.builder()     
                .userDTO(userDTO)
                .refreshToken("refreshToken123")
                .build();


        updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setEmail("updatedEmail@exeter.ac.uk");
        updateUserPasswordDTO = new UpdateUserPasswordDTO("oldPassword","newPassword");
    }


    /**
     * test successful user registration
     * @throws Exception
     */
    @Test
    public void testRegisterUser_Success() throws Exception{

        //DTO -> JSON string
        String requestBody = objectMapper.writeValueAsString(createUserDTO);
        when(userService.createUser(any(CreateUserDTO.class))).thenReturn(userRegisterDTO);


        //http POST to controller, and ensure a response is given
        mockMvc.perform(post("/users/register")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken", "refreshToken123"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                //these should match the saved output. $. is the path
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.username").value("testUsername"))
                .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));

        verify(userService).createUser(any(CreateUserDTO.class));

    }

    /**
     * test registering a user with an email that's already in use
     * should throw exception
     * @throws Exception
     */
    @Test
    public void testRegisterUser_EmailTaken() throws Exception {

        String requestBody = objectMapper.writeValueAsString(createUserDTO);

        when(userService.createUser(any(CreateUserDTO.class))).thenThrow(new EmailAlreadyTakenException());
        
        mockMvc.perform(post("/users/register")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());

        verify(userService).createUser(any(CreateUserDTO.class));

    }


    /**
     * Test a successful user login
     * @throws Exception
     */
    @Test
    public void testUserLogin_Success() throws Exception{

        String requestBody = objectMapper.writeValueAsString(loginDTO);


        when(userService.userLogin(any(LoginDTO.class))).thenReturn(userRegisterDTO);
        
        mockMvc.perform(post("/users/login")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("refreshToken"))
            .andExpect(jsonPath("$.id").value(testUserId.toString()))
            .andExpect(jsonPath("$.username").value("testUsername"))
            .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));


    }

    /**
     * Tests unsuccessful user login as user doesnt exist
     * @throws Exception
     */
    @Test
    public void testUserLogin_UserNotFound() throws Exception{
        String requestBody = objectMapper.writeValueAsString(loginDTO);

        when(userService.userLogin(any(LoginDTO.class))).thenThrow(new UserNotFoundException());
        
        mockMvc.perform(post("/users/login")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests unsuccessful user login as password is incorrect
     * @throws Exception
     */
    @Test
    public void testUserLogin_PasswordIncorrect() throws Exception{
        String requestBody = objectMapper.writeValueAsString(loginDTO);

        when(userService.userLogin(any(LoginDTO.class))).thenThrow(new PasswordIncorrectException());
        
        mockMvc.perform(post("/users/login")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

    }


    /**
     * tests getting a user profile after authentication
     * @throws Exception
     */
    @Test
    public void testGetUserProfile_Success() throws Exception{

        // String requestBody = objectMapper.writeValueAsString(loginDTO);

        when(userService.getUserProfile(anyString())).thenReturn(userDTO);
        mockMvc.perform(get("/users/me")
                    .header("Authorization", "Bearer accessToken123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testUserId.toString()))
            .andExpect(jsonPath("$.username").value("testUsername"))
            .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));

    }


    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetUserProfile_UserNotFound() throws Exception{
        String requestBody = objectMapper.writeValueAsString(loginDTO);

        when(userService.getUserProfile(anyString())).thenThrow(new UserNotFoundException());
        
        mockMvc.perform(get("/users/me")
                    .header("Authorization", "Bearer accessToken123"))
            .andExpect(status().isNotFound());
           
            // .andExpect(cookie().exists("refreshToken"))
            // .andExpect(jsonPath("$.id").value(testUserId.toString()))
            // .andExpect(jsonPath("$.username").value("testUsername"))
            // .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));

    }



    

  
    @Test
    public void testPassword_Sucess() throws Exception{

        String requestBody = objectMapper.writeValueAsString(updateUserPasswordDTO);


        doNothing().when(userService).updateUserPassword(anyString(), any(UpdateUserPasswordDTO.class));

        mockMvc.perform(patch("/users/password")
                    .header("Authorization", "Bearer accessToken123")
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
        //     .andExpect(jsonPath("$.id").value(testUserId.toString()))
        //     .andExpect(jsonPath("$.username").value("testUsername"))
        //     .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));

        verify(userService).updateUserPassword(eq("accessToken123"), any(UpdateUserPasswordDTO.class));

    }





    // @Test
    // public void testPassword_WrongPassword() throws Exception{
    //     String requestBody = objectMapper.writeValueAsString(updateUserPasswordDTO);

    //     when(userService.updateUserPassword(any(UpdateUserPassword.class))).thenThrow(new PasswordIncorrectException());
        
    //     mockMvc.perform(patch("/users/password")
    //                 .header("Authorization", "Bearer accessToken123")   
    //                 .content(requestBody)
    //                 .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isBadRequest());
           
           
           
    //         // .andExpect(cookie().exists("refreshToken"))
    //         // .andExpect(jsonPath("$.id").value(testUserId.toString()))
    //         // .andExpect(jsonPath("$.username").value("testUsername"))
    //         // .andExpect(jsonPath("$.email").value("test@exeter.ac.uk"));

    // }


    //tests unexpected runtime error, should be a 500 error
    @Test
    public void testPassword_500() throws Exception{

        String requestBody = objectMapper.writeValueAsString(updateUserPasswordDTO);

        doThrow(new RuntimeException("Database error")).when(userService).updateUserPassword(anyString(), any(UpdateUserPasswordDTO.class));

        mockMvc.perform(patch("/users/password")
                .header("Authorization", "Bearer accessToken123")   
                    .content(requestBody)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService).updateUserPassword(anyString(), any(UpdateUserPasswordDTO.class));

    }


}