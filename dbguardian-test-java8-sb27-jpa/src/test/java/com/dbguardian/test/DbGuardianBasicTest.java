package com.dbguardian.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * DBGuardian 基础测试 - JPA 版本
 */
@SpringBootTest
@ActiveProfiles("test")
class DbGuardianBasicTest {

    @Test
    void contextLoads() {
        // 验证上下文加载成功
        Application application = new Application();
        assertNotNull(application);
    }
}