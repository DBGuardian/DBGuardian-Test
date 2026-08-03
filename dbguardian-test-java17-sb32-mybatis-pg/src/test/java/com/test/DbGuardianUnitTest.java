package com.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbGuardianUnitTest {

    @Test
    void createsReadAndWriteContexts() {
        RoutingContext read = new RoutingContext();
        read.setOperation("read");
        RoutingContext write = new RoutingContext();
        write.setOperation("write");

        assertEquals("read", read.getOperation());
        assertEquals("write", write.getOperation());
        assertFalse(read.isForceMaster());
    }

    @Test
    void storesAndClearsContext() {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        context.setForceMaster(true);
        RoutingContextHolder.set(context);
        try {
            assertTrue(RoutingContextHolder.get().isForceMaster());
        } finally {
            RoutingContextHolder.clear();
        }
        assertNull(RoutingContextHolder.get());
    }
}