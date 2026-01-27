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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Creates a new User")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            UserRegisterDTO userRegisterDTO = userService.createUser(createUserDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", userRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userRegisterDTO.getUserDTO());
        }

        catch (UsernameAlreadyTakenException | EmailAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Allows a User to Login")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            UserRegisterDTO userRegisterDTO = userService.userLogin(loginDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", userRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
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

    @Operation(summary = "Allows a User to get their own profile")
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfile(@NotBlank @RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");

            UserDTO userDTO = userService.getUserProfile(accessToken);
            return ResponseEntity.ok(userDTO);
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Allows a user to update their user details")
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

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Allows a User to change their password")
    @PatchMapping("/password")
    public ResponseEntity<?> updateUserPassword(@RequestHeader("Authorization") String authHeader,
                                                @Valid @RequestBody UpdateUserPasswordDTO passwordDTO) {

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            userService.updateUserPassword(accessToken, passwordDTO);
            return ResponseEntity.noContent().build();
        }

        catch (PasswordIncorrectException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

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

}
