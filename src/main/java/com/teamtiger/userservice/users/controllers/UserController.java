package com.teamtiger.userservice.users.controllers;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.exceptions.UsernameAlreadyTakenException;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.services.UserService;
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            CreatedUserDTO createdUserDTO = userService.createUser(createUserDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", createdUserDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();

            //Remove the refresh token from response body
            UserDTO userDTO = UserDTO.builder()
                    .id(createdUserDTO.getId())
                    .email(createdUserDTO.getEmail())
                    .username(createdUserDTO.getUsername())
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userDTO);
        }

        catch (UsernameAlreadyTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            CreatedUserDTO createdUserDTO = userService.userLogin(loginDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", createdUserDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();

            //Remove the refresh token from response body
            UserDTO userDTO = UserDTO.builder()
                    .id(createdUserDTO.getId())
                    .email(createdUserDTO.getEmail())
                    .username(createdUserDTO.getUsername())
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userDTO);
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

    @PatchMapping("/me")
    public ResponseEntity<?> updateUserProfile(@NotBlank @RequestHeader("Authorization") String authHeader,
                                               @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");

            CreatedUserDTO createdUserDTO = userService.updateUserProfile(accessToken, updateUserDTO);

            //If username has changed then issue new refresh token to match
            if(createdUserDTO.getRefreshToken() == null) {
                UserDTO userDTO = UserDTO.builder()
                        .id(createdUserDTO.getId())
                        .username(createdUserDTO.getUsername())
                        .email(createdUserDTO.getEmail())
                        .build();
                return ResponseEntity.ok(userDTO);
            }

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", createdUserDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();

            //Remove the refresh token from response body
            UserDTO userDTO = UserDTO.builder()
                    .id(createdUserDTO.getId())
                    .email(createdUserDTO.getEmail())
                    .username(createdUserDTO.getUsername())
                    .build();


            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(userDTO);


        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
