package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.models.*;

public interface UserService {

    CreatedUserDTO createUser(CreateUserDTO userDTO);

    CreatedUserDTO userLogin(LoginDTO loginDTO);

    UserDTO getUserProfile(String accessToken);

    CreatedUserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO);

}
