package com.dbguardian.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 路由上下文测试
 */
class DataSourceContextHolderTest {

    @AfterEach
    void tearDown() {
        RoutingContextHolder.clear();
    }

    @Test
    void testSetAndGetRoutingContext() {
        RoutingContext context = new RoutingContext();
        context.setOrmType("jpa");
        context.setOperation("read");
        
        RoutingContextHolder.set(context);
        
        RoutingContext retrieved = RoutingContextHolder.get();
        assertNotNull(retrieved);
        assertEquals("jpa", retrieved.getOrmType());
        assertEquals("read", retrieved.getOperation());
    }

    @Test
    void testClear() {
        RoutingContextHolder.set(new RoutingContext());
        RoutingContextHolder.clear();
        
        RoutingContext context = RoutingContextHolder.get();
        assertNull(context, "clear 后上下文应为 null");
    }
}