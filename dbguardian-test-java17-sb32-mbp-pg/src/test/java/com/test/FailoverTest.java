package com.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class FailoverTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("datasourceCoordinationService")
    private Object coordinationService;

    @Test
    public void testFailoverBeansAvailable() {
        assertTrue(applicationContext.containsBean("datasourceCoordinationService"));
        assertTrue(applicationContext.containsBean("failoverController"));
        assertTrue(applicationContext.containsBean("dataSourceHealthChecker"));
    }

    @Test
    public void testPostgreSqlCoordinationStatusAvailable() throws Exception {
        assertNotNull(coordinationService, "PostgreSQL 协调服务应完成装配");
        Object status = coordinationService.getClass()
                .getMethod("getCoordinationStatus")
                .invoke(coordinationService);
        assertNotNull(status, "协调状态不应为空");
        assertNotNull(status.getClass().getMethod("getMasterStatus").invoke(status),
                "主库状态不应为空");
    }
}