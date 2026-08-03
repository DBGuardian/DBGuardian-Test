package com.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataSourceContextHolderTest {
    @Test
    public void testContextLifecycle() {
        RoutingContextHolder.clear();
        assertNull(RoutingContextHolder.get());

        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        context.setTransactional(true);
        RoutingContextHolder.set(context);
        try {
            assertEquals("read", RoutingContextHolder.get().getOperation());
            assertTrue(RoutingContextHolder.get().isTransactional());
        } finally {
            RoutingContextHolder.clear();
        }
        assertNull(RoutingContextHolder.get());
    }
}