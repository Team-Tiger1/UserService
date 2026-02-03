package com.teamtiger.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "jwt.secret=bm90LXRoZS1yZWFsLXNlY3JldC1idXQtbWFrZS1pdC1sb25nLWVub3VnaC0yNTYtYml0cw==")
@Transactional
@ActiveProfiles("test")
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
