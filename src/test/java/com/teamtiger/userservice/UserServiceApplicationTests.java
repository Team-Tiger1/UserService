package com.teamtiger.userservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "jwt.secret=bm90LXRoZS1yZWFsLXNlY3JldC1idXQtbWFrZS1pdC1sb25nLWVub3VnaC0yNTYtYml0cw==")
@Transactional
@ActiveProfiles("test")
@Suite
@EnableCaching
@SelectPackages("com.teamtiger.userservice")
class UserServiceApplicationTests {


}
