package com.teamtiger.userservice.auth.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceJPA implements AuthService{

    private final UserRepository userRepository;
//    private final VendorRepository vendorRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AccessTokenDTO getAccessToken(String refreshToken) {

        //Extract username from JWT token
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        String role = jwtTokenUtil.getRoleFromToken(refreshToken); //Users only have 1 role

        //Check username existence against DB
        if(role.equals("USER")) {
            boolean doesUsernameExist = userRepository.existsByUsername(username);
            if(!doesUsernameExist) {
                throw new UserNotFoundException();
            }
        } else if (role.equals("VENDOR")) {
//            boolean doesCompanyExist = vendorRepository.existsByName(username);
        }




        //Generate new access token
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .accessToken(jwtTokenUtil.generateAccessToken(username))
                .build();
        return accessTokenDTO;
    }
}
