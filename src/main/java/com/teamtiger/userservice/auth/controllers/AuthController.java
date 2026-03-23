package com.teamtiger.userservice.auth.controllers;

import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import com.teamtiger.userservice.auth.services.AuthServiceJPA;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceJPA authServiceJPA;

    /**
     * Method for Users and Vendors to refresh their access token using their refresh token
     * @param refreshToken Bearer token that the client stores for a long period of time
     * @return A ResponseEntity with the new access token in the body,
     * returns 404 if the User or Vendor doesn't exist,
     * returns 500 if a different error occurs
     */
    @Operation(summary = "Creates an access token given a refresh token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccessTokenDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or vendor not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@NotBlank @CookieValue("refreshToken") String refreshToken) {
        try {
            AccessTokenDTO accessTokenDTO = authServiceJPA.getAccessToken(refreshToken);
            return ResponseEntity.ok(accessTokenDTO);
        }

        catch (UserNotFoundException | CompanyNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }




}
