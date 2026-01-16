package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;

@Data
public class UpdateUserDTO {

    @Size(min = UserConstants.MIN_USERNAME_LENGTH, max = UserConstants.MAX_USERNAME_LENGTH)
    private String username;

    @Email
    private String email;

}
