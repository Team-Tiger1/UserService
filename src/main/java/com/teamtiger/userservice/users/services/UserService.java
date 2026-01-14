package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.CreatedUserDTO;
import com.teamtiger.userservice.users.models.LoginDTO;
import com.teamtiger.userservice.users.models.UserDTO;

public interface UserService {

    CreatedUserDTO createUser(CreateUserDTO userDTO);

    CreatedUserDTO userLogin(LoginDTO loginDTO);

}
