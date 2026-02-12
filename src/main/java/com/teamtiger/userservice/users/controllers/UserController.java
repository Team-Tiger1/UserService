package com.teamtiger.userservice.users.controllers;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.exceptions.*;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Processes a User's request to register
     * @param createUserDTO A valid request body with the required details
     * @return A ResponseEntity that returns 200 if successful and sets a refresh cookie on the client's device
     * 409 if the email used already has an account
     * 500 if a different error occurs
     */
    @Operation(summary = "Creates a new User")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            UserRegisterDTO userRegisterDTO = userService.createUser(createUserDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", userRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userRegisterDTO.getUserDTO());
        }

        catch (EmailAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a User's request to Login
     * @param loginDTO A valid request body with details to login with
     * @return A ResponseEntity that returns 200 if successful and sets a refresh cookie on the client's device
     * 404 if the user was not found in the database
     * 401 if the password given was incorrect
     * 500 if a different error occurs
     */
    @Operation(summary = "Allows a User to Login")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            UserRegisterDTO userRegisterDTO = userService.userLogin(loginDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", userRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userRegisterDTO.getUserDTO());
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (PasswordIncorrectException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a User's request to get their profile details
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 200 with the user's details
     * 404 if the user was not found
     * 401 if a vendor tries to access the endpoint
     * 500 if a different error occurred
     */
    @Operation(summary = "Allows a User to get their own profile")
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");

            UserDTO userDTO = userService.getUserProfile(accessToken);
            return ResponseEntity.ok(userDTO);
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a Users request to update their email address
     * @param authHeader A bearer access token
     * @param updateUserDTO A wrapper for the valid email address
     * @return A ResponseEntity that returns 200 if the email was updated successfully
     * 404 if the User was not found
     * 401 if a vendor tries to access the endpoint
     * 500 if a different error occurred
     */
    @Operation(summary = "Allows a user to update their email address")
    @PatchMapping("/me")
    public ResponseEntity<?> updateUserProfile(@NotBlank @RequestHeader("Authorization") String authHeader,
                                               @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");

            UserDTO userDTO = userService.updateUserProfile(accessToken, updateUserDTO);

            return ResponseEntity.ok(userDTO);
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a User's request to update their password
     * @param authHeader A bearer access token
     * @param passwordDTO A new valid password and their old password
     * @return A ResponseEntity that returns 200 if the password was updated successfully
     * 401 if the old password entered was incorrect, or a vendor tries to access the endpoint
     * 404 if the user was not found
     * 500 if a different error occurred
     */
    @Operation(summary = "Allows a User to change their password")
    @PatchMapping("/password")
    public ResponseEntity<?> updateUserPassword(@RequestHeader("Authorization") String authHeader,
                                                @Valid @RequestBody UpdateUserPasswordDTO passwordDTO) {

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            userService.updateUserPassword(accessToken, passwordDTO);
            return ResponseEntity.noContent().build();
        }

        catch (PasswordIncorrectException | AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a User's request to get their current streak
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 200 with the user streak
     * 401 if a vendor tries to access the endpoint
     * 500 if a different error occurs
     */
    @Operation(summary = "Get a streak for a user")
    @GetMapping("/streak")
    public ResponseEntity<?> getUserStreak(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            StreakDTO streakDTO = userService.getUserStreak(accessToken);
            return ResponseEntity.ok(streakDTO);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes bulk data that is seeded, and stores it in the database
     * @param authToken A bearer access token
     * @param users A list of generated 'fake' users
     * @return A ResponseEntity that returns 204 if successful
     * 401 if a User or Vendor tries to access the endpoint (only INTERNAL is allowed)
     * 500 if a different error occurs
     */
    @Operation(summary = "Allows for bulk data transfer for seeded data")
    @PostMapping("/internal")
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authToken, @Valid @RequestBody List<UserSeedDTO> users) {
        try {
            String token = authToken.replace("Bearer ", "");
            userService.loadSeededUsers(token, users);
            return ResponseEntity.noContent().build();
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
