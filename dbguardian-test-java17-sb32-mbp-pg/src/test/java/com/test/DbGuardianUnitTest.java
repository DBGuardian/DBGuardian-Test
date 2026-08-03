package com.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DbGuardianUnitTest {
    @Test
    public void testRoutingFlags() {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        context.setForceMaster(true);
        context.setReadOnlyTransaction(true);
        RoutingContextHolder.set(context);
        try {
            assertEquals("read", RoutingContextHolder.get().getOperation());
            assertTrue(RoutingContextHolder.get().isForceMaster());
            assertTrue(RoutingContextHolder.get().isReadOnlyTransaction());
        } finally {
            RoutingContextHolder.clear();
        }
    }
}