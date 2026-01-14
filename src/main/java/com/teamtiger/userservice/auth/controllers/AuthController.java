package com.teamtiger.userservice.auth.controllers;

import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import com.teamtiger.userservice.auth.services.AuthServiceJPA;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceJPA authServiceJPA;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@NotBlank @CookieValue("refreshToken") String refreshToken) {
        try {
            AccessTokenDTO accessTokenDTO = authServiceJPA.getAccessToken(refreshToken);
            return ResponseEntity.ok(accessTokenDTO);
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }




}
