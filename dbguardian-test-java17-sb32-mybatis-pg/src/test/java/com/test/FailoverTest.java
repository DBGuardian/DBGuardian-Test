package com.test;

import io.dbguardian.spring.failover.FailoverController;
import io.dbguardian.spring.runtime.ClusterRuntimeStateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class FailoverTest {

    @Autowired
    private FailoverController failoverController;

    @Autowired
    private ClusterRuntimeStateManager runtimeStateManager;

    @Test
    void exposesPostgreSqlFailoverComponents() {
        assertNotNull(failoverController);
        assertNotNull(runtimeStateManager.current());
    }
}