package com.teamtiger.userservice.users.models;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for wrapping UserDTO and refresh token, when users register an account
 */
@Data
@Builder
public class UserRegisterDTO {
    private UserDTO userDTO;
    private String refreshToken;

}
