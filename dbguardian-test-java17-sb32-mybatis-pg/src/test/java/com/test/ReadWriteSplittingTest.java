package com.test;

import com.test.mapper.UserMapper;
import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class ReadWriteSplittingTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void providesMyBatisMapperForPostgreSql() {
        assertNotNull(userMapper);
    }

    @Test
    void marksReadContextForReplicaRouting() {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        RoutingContextHolder.set(context);
        try {
            assertEquals("read", RoutingContextHolder.get().getOperation());
        } finally {
            RoutingContextHolder.clear();
        }
    }

    @Test
    void forcesWriteContextToCurrentPrimary() {
        RoutingContext context = new RoutingContext();
        context.setOperation("write");
        context.setForceMaster(true);
        RoutingContextHolder.set(context);
        try {
            assertTrue(RoutingContextHolder.get().isForceMaster());
        } finally {
            RoutingContextHolder.clear();
        }
    }
}