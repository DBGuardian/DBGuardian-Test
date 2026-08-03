package com.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import com.test.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class ReadWriteSplittingTest {
    @Autowired(required = false)
    private UserMapper userMapper;

    @Test
    public void testMapperInjected() {
        assertNotNull(userMapper, "MyBatis-Plus Mapper 应该由 Spring 注入");
    }

    @Test
    public void testReadContextRoutesToSlave() {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        RoutingContextHolder.set(context);
        try {
            assertEquals("read", RoutingContextHolder.get().getOperation());
            assertFalse(RoutingContextHolder.get().isForceMaster());
        } finally {
            RoutingContextHolder.clear();
        }
    }

    @Test
    public void testWriteContextRoutesToMaster() {
        RoutingContext context = new RoutingContext();
        context.setOperation("write");
        RoutingContextHolder.set(context);
        try {
            assertEquals("write", RoutingContextHolder.get().getOperation());
        } finally {
            RoutingContextHolder.clear();
        }
    }

    @Test
    public void testForceMasterForRead() {
        RoutingContext context = new RoutingContext();
        context.setOperation("read");
        context.setForceMaster(true);
        RoutingContextHolder.set(context);
        try {
            assertTrue(RoutingContextHolder.get().isForceMaster());
        } finally {
            RoutingContextHolder.clear();
        }
    }
}