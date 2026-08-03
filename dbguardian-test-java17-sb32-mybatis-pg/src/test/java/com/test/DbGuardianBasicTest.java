package com.test;

import com.test.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class DbGuardianBasicTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void loadsPostgreSqlMyBatisApplicationContext() {
        assertNotNull(userMapper);
    }
}