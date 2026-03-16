package com.teamtiger.userservice.vendors.controllers;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.exceptions.AuthorizationException;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    /**
     * Processes a Vendors's request to register
     * @param createVendorDTO A valid request body with the required details
     * @return A ResponseEntity that returns 200 if successful and sets a refresh cookie on the client's device
     * 409 if the company name is already taken
     * 500 if a different error occurs
     */
    @Operation(summary = "Allows a Vendor to create an account")
    @PostMapping("/register")
    public ResponseEntity<?> registerVendor(@Valid @RequestBody CreateVendorDTO createVendorDTO) {
        try {

            VendorRegisterDTO vendorRegisterDTO = vendorService.createVendor(createVendorDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", vendorRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true) //CHANGE TO TRUE FOR PRODUCTION
                    .sameSite("None")
                    .path("/")
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

    /**
     * Processes a Vendors's request to Login
     * @param loginVendorDTO A valid request body with details to login with
     * @return A ResponseEntity that returns 200 if successful and sets a refresh cookie on the client's device
     * 404 if the vendor was not found in the database
     * 401 if the password given was incorrect
     * 500 if a different error occurs
     */
    @Operation(summary = "Allows a Vendor to login")
    @PostMapping("/login")
    public ResponseEntity<?> loginVendor(@Valid @RequestBody LoginVendorDTO loginVendorDTO) {

        try {
            VendorRegisterDTO vendorRegisterDTO = vendorService.loginVendor(loginVendorDTO);

            //Create the Cookie
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", vendorRegisterDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
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

    /**
     * Processes a Vendors request to update their details
     * @param authHeader A bearer access token
     * @param updateVendorDTO A valid request body with the details they want to update
     * @return A ResponseEntity that returns 200 if the details were updated successfully
     * 404 if the vendor was not found
     * 401 if a user tries to access the endpoint
     * 500 if a different error occurred
     */
    @Operation(summary = "Allows a Vendor to update their account details")
    @PatchMapping("/me")
    public ResponseEntity<?> updateVendorDetails(@RequestHeader("Authorization") String authHeader,
                                                 @Valid @RequestBody UpdateVendorDTO updateVendorDTO) {

        try {

            String accessToken = authHeader.replace("Bearer ", "");
            VendorDTO vendorDTO = vendorService.updateVendorDetails(updateVendorDTO, accessToken);

            return ResponseEntity.ok(vendorDTO);

        }

        catch (CompanyNotFoundException e) {
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
     * Processes a Vendors's request to update their password
     * @param authHeader A bearer access token
     * @param passwordDTO A new valid password and their old password
     * @return A ResponseEntity that returns 200 if the password was updated successfully
     * 401 if the old password entered was incorrect, or a vendor tries to access the endpoint
     * 404 if the vendor was not found
     * 500 if a different error occurred
     */
    @Operation(summary = "Allows a Vendor to update their password")
    @PatchMapping("/password")
    public ResponseEntity<?> updateVendorPassword(@RequestHeader("Authorization") String authHeader,
                                                  UpdateVendorPasswordDTO passwordDTO) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            vendorService.updatePassword(passwordDTO, accessToken);
            return ResponseEntity.noContent().build();
        }

        catch(CompanyNotFoundException e) {
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
     * Processes a Vendor's request to get their profile details
     * @param authHeader A bearer access token
     * @return A ResponseEntity that returns 200 with the vendor's details
     * 404 if the vendor was not found
     * 401 if a user tries to access the endpoint
     * 500 if a different error occurred
     */
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
     * @param vendors A list of generated 'fake' vendors
     * @return A ResponseEntity that returns 204 if successful
     * 401 if a User or Vendor tries to access the endpoint (only INTERNAL is allowed)
     * 500 if a different error occurs
     */
    @Operation(summary = "Allows bulk data transfer for seeded data")
    @PostMapping("/internal")
    public ResponseEntity<?> loadSeededData(@RequestHeader("Authorization") String authToken, @RequestBody List<VendorSeedDTO> vendors) {
        try {
            String accessToken = authToken.replace("Bearer ", "");
            vendorService.loadSeededData(accessToken, vendors);
            return ResponseEntity.noContent().build();

        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Endpoint for getting basic details about lots of vendors
     * @return ResponseEntity returns 200 with a list of vendor information
     * 500 if an error occurs
     */
    @Operation(summary = "Gets all vendors basic info")
    @GetMapping
    public ResponseEntity<?> getAllVendors() {
        try {
            List<BasicVendorDTO> vendors = vendorService.getAllVendors();
            return ResponseEntity.ok(vendors);
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Gets detailed information about a specific vendor
     * @param vendorId A UUID of a vendor record
     * @return ResponseEntity returns 200 with detailed vendor information
     * 404 if the vendor is not found
     * 500 if a different error occurs
     */
    @Operation(summary = "Get Detailed Vendor Info")
    @GetMapping("/{vendorId}")
    public ResponseEntity<?> getVendorInfo(@PathVariable UUID vendorId) {
        try {
            VendorDTO vendorDTO = vendorService.getDetailedVendorInfo(vendorId);
            return ResponseEntity.ok(vendorDTO);
        }

        catch (CompanyNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Processes a vendors request to get all their disputes
     * @param authHeader The Authorization Header
     * @return A list of disputes
     */
    @Operation(summary = "Allows a vendor to get all their disputes")
    @GetMapping("/disputes")
    public ResponseEntity<?> getVendorDisputes(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            List<DisputeDTO> disputeDTOS = vendorService.getAllDisputes(accessToken);
            return ResponseEntity.ok(disputeDTOS);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Proceses a vendors request to update a dispute
     * @param authHeader The authorization header
     * @param updateDisputeDTO The new status and response for the dispute
     * @return The Updated Dispute
     */
    @Operation(summary = "Allows a vendor to update a dispute against them")
    @PostMapping("/disputes")
    public ResponseEntity<?> updateDispute(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody UpdateDisputeDTO updateDisputeDTO) {

        try {
            String accessToken = authHeader.replace("Bearer ", "");
            DisputeDTO disputeDTO = vendorService.updateDispute(accessToken, updateDisputeDTO);
            return ResponseEntity.ok(disputeDTO);
        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Processes a vendors request to delete their account
     * @param authHeader Authorization Header
     * @return 204 No Content, or 401 if the role is not VENDOR
     */
    @Operation(summary = "Allows a vendor to delete their account")
    @DeleteMapping
    public ResponseEntity<?> deleteVendor(@RequestHeader("Authorization") String authHeader) {
        try {
            String accessToken = authHeader.replace("Bearer ", "");
            vendorService.deleteVendor(accessToken);

            //Set refresh cookie to expire immediately
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", null)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/")
                    .maxAge(0)
                    .build();

            return ResponseEntity.noContent()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .build();

        }

        catch (AuthorizationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }



}
