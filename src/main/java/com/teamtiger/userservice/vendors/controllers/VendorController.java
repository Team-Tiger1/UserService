package com.teamtiger.userservice.vendors.controllers;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNameTakenException;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.models.CreateVendorDTO;
import com.teamtiger.userservice.vendors.models.LoginVendorDTO;
import com.teamtiger.userservice.vendors.models.VendorRegisterDTO;
import com.teamtiger.userservice.vendors.services.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;


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



}
