package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class CoordinationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testCoordinationBeans() {
        assertNotNull(applicationContext);
        assertTrue(applicationContext.containsBean("datasourceCoordinationService"));
        assertTrue(applicationContext.containsBean("failoverController"));
        assertTrue(applicationContext.containsBean("dataSourceHealthChecker"));
    }
}