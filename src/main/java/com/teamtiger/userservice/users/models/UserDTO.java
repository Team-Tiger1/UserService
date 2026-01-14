package com.teamtiger.userservice.users.models;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
}
