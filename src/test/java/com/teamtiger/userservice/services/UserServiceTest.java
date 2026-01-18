package com.teamtiger.userservice.services;

import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.UserRegisterDTO;
import com.teamtiger.userservice.users.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "jwt.secret=bm90LXRoZS1yZWFsLXNlY3JldC1idXQtbWFrZS1pdC1sb25nLWVub3VnaC0yNTYtYml0cw==")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterUser() {
        CreateUserDTO createUserDTO = CreateUserDTO.builder()
                .email("example@exeter.ac.uk")
                .username("example1")
                .password("password1")
                .build();

        UserRegisterDTO userRegisterDTO = userService.createUser(createUserDTO);
        Assertions.assertFalse(userRegisterDTO.getRefreshToken().isEmpty());

    }


}
