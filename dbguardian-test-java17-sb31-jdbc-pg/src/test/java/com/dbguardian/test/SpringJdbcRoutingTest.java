package com.dbguardian.test;

import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import io.dbguardian.spring.aspect.SpringJdbcDataSourceAdvisor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class SpringJdbcRoutingTest {

    @AfterEach
    void clearRoutingContext() {
        RoutingContextHolder.clear();
    }

    @Test
    void queryOperationUsesSpringJdbcReadContext() {
        JdbcOperations target = mock(JdbcOperations.class);
        doAnswer(invocation -> {
            assertRouting("spring-jdbc", "read");
            return 1;
        }).when(target).queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Integer.class));

        JdbcOperations proxy = createProxy(target);
        assertEquals(Integer.valueOf(1), proxy.queryForObject("SELECT 1", Integer.class));
        assertNull(RoutingContextHolder.get(), "调用结束后必须清理路由上下文");
    }

    @Test
    void updateOperationUsesSpringJdbcWriteContext() {
        JdbcOperations target = mock(JdbcOperations.class);
        doAnswer(invocation -> {
            assertRouting("spring-jdbc", "write");
            return 1;
        }).when(target).update(anyString());

        JdbcOperations proxy = createProxy(target);
        assertEquals(1, proxy.update("DELETE FROM t_user"));
        assertNull(RoutingContextHolder.get(), "调用结束后必须清理路由上下文");
    }

    private JdbcOperations createProxy(JdbcOperations target) {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvisor(new SpringJdbcDataSourceAdvisor());
        return (JdbcOperations) proxyFactory.getProxy();
    }

    private void assertRouting(String ormType, String operation) {
        RoutingContext context = RoutingContextHolder.get();
        assertEquals(ormType, context.getOrmType());
        assertEquals(operation, context.getOperation());
    }
}
