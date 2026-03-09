package com.teamtiger.userservice.users.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for wrapping UserDTO and refresh token, when users register an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {
    private UserDTO userDTO;
    private String refreshToken;

}
