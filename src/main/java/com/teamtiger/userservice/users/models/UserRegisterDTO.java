package com.teamtiger.userservice.users.models;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserRegisterDTO {
    private UserDTO userDTO;
    private String refreshToken;

}
