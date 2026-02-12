package com.teamtiger.userservice.auth.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.repositories.UserRepository;
import com.teamtiger.userservice.vendors.exceptions.CompanyNotFoundException;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceJPA implements AuthService{

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Method that generates a new access token for a user or vendor
     * It extracts the Id from the refresh token and checks the database
     * Then it generates the new access token
     * @param refreshToken The bearer token that is stored on the clients device in the long-term
     * @return An AccessTokenDTO which has the new access token
     */
    @Override
    @Transactional
    public AccessTokenDTO getAccessToken(String refreshToken) {

        //Extract username and role from JWT token
        UUID uuid = jwtTokenUtil.getUuidFromToken(refreshToken);
        String role = jwtTokenUtil.getRoleFromToken(refreshToken); //Users only have 1 role

        //Check username existence against DB
        if(role.equals("USER")) {
            boolean doesUserExists = userRepository.existsById(uuid);
            System.out.println(uuid + " " + doesUserExists);
            if(!doesUserExists) {
                throw new UserNotFoundException();
            }
        } else if (role.equals("VENDOR")) {
            boolean doesCompanyExist = vendorRepository.existsById(uuid);
            if(!doesCompanyExist) {
                throw new CompanyNotFoundException();
            }
        }

        //Generate new access token
        return AccessTokenDTO.builder()
                .accessToken(jwtTokenUtil.generateAccessToken(uuid, Role.valueOf(role)))
                .build();
    }
}
