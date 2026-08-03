package com.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourceContextHolderTest {

    @Test
    void keepsContextIsolatedPerThread() throws InterruptedException {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        RoutingContextHolder.set(context);
        try {
            Thread child = new Thread(() -> assertNull(RoutingContextHolder.get()));
            child.start();
            child.join();
            assertEquals("read", RoutingContextHolder.get().getOperation());
        } finally {
            RoutingContextHolder.clear();
        }
    }

    @Test
    void replacesContextForWriteOperation() {
        RoutingContext context = new RoutingContext();
        context.setOperation("write");
        RoutingContextHolder.set(context);
        try {
            assertEquals("write", RoutingContextHolder.get().getOperation());
        } finally {
            RoutingContextHolder.clear();
        }
    }
}