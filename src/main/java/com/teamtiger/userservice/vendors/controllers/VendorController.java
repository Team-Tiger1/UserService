package com.teamtiger.userservice.vendors.controllers;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.exceptions.CompanyNameTakenException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.models.*;
import com.teamtiger.userservice.vendors.services.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @Operation(summary = "Allows a Vendor to create an account")
    @PostMapping("/register")
    public ResponseEntity<?> registerVendor(@Valid @RequestBody CreateVendorDTO createVendorDTO) {
        try {

            VendorRegisterDTO vendorRegisterDTO = vendorService.createVendor(createVendorDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", vendorRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(vendorRegisterDTO.getVendorDTO());

        }

        catch (CompanyNameTakenException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }


        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Allows a Vendor to login")
    @PostMapping("/login")
    public ResponseEntity<?> loginVendor(@Valid @RequestBody LoginVendorDTO loginVendorDTO) {

        try {
            VendorRegisterDTO vendorRegisterDTO = vendorService.loginVendor(loginVendorDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", vendorRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/api/auth/refresh")
                    .maxAge(JwtTokenUtil.REFRESH_TOKEN_EXPIRY)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(vendorRegisterDTO.getVendorDTO());

        }

        catch (CompanyNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (PasswordIncorrectException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Allows a Vendor to update their account details")
    @PatchMapping("/me")
    public ResponseEntity<?> updateVendorDetails(@RequestHeader("Authorization") String token,
                                                 @Valid @RequestBody UpdateVendorDTO updateVendorDTO) {

        try {

            String accessToken = token.replace("Bearer ", "");
            VendorDTO vendorDTO = vendorService.updateVendorDetails(updateVendorDTO, accessToken);

            return ResponseEntity.ok(vendorDTO);

        }

        catch (CompanyNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Allows a Vendor to update their password")
    @PatchMapping("/password")
    public ResponseEntity<?> updateVendorPassword(@RequestHeader("Authorization") String token,
                                                  UpdateVendorPasswordDTO passwordDTO) {
        try {
            String accessToken = token.replace("Bearer ", "");
            vendorService.updatePassword(passwordDTO, accessToken);
            return ResponseEntity.noContent().build();
        }

        catch(CompanyNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @Operation(summary = "Allows a vendor to fetch their own profile")
    @GetMapping("/me")
    public ResponseEntity<?> getOwnVendorProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            VendorDTO vendorDTO = vendorService.getVendorProfile(accessToken);
            return ResponseEntity.ok(vendorDTO);
        }

        catch (CompanyNotFoundException e)  {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



}
