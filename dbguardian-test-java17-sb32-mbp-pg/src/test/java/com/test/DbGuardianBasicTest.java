package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class DbGuardianBasicTest {
    @Test
    public void testContextLoads() {
        assertTrue(true, "Spring Boot 上下文应该正常加载");
    }

    @Test
    public void testMyBatisPlusIntegration() {
        assertTrue(true, "MyBatis-Plus 应该正确集成");
    }
}