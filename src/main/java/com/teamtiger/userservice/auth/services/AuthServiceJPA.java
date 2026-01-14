package com.teamtiger.userservice.auth.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceJPA implements AuthService{

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AccessTokenDTO getAccessToken(String refreshToken) {

        //Extract username from JWT token
        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

        //Check username existence against DB
        boolean doesUsernameExist = userRepository.existsByUsername(username);
        if(!doesUsernameExist) {
            throw new UsernameNotFoundException("Username not found");
        }

        //Generate new access token
        AccessTokenDTO accessTokenDTO = AccessTokenDTO.builder()
                .accessToken(jwtTokenUtil.generateAccessToken(username))
                .build();
        return accessTokenDTO;
    }
}
