package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.models.*;

public interface UserService {

    UserRegisterDTO createUser(CreateUserDTO userDTO);

    UserRegisterDTO userLogin(LoginDTO loginDTO);

    UserDTO getUserProfile(String accessToken);

    UserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO);

    void updateUserPassword(String accessToken, UpdateUserPasswordDTO passwordDTO);

}
