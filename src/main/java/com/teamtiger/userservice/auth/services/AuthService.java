package com.teamtiger.userservice.auth.services;

import com.teamtiger.userservice.auth.models.AccessTokenDTO;

public interface AuthService {

    AccessTokenDTO getAccessToken(String refreshToken);

}
