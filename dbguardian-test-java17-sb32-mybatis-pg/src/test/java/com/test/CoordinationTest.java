package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class CoordinationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void registersCoordinationAndRuntimeBeans() {
        assertTrue(applicationContext.containsBean("datasourceCoordinationService"));
        assertTrue(applicationContext.containsBean("dataSourceHealthChecker"));
        assertTrue(applicationContext.containsBean("dataSourceRegistry"));
        assertTrue(applicationContext.containsBean("failoverController"));
    }
}