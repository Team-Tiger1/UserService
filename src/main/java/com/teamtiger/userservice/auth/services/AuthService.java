package com.teamtiger.userservice.auth.services;

import com.teamtiger.userservice.auth.models.AccessTokenDTO;
import org.springframework.stereotype.Service;

public interface AuthService {

    AccessTokenDTO getAccessToken(String refreshToken);

}
