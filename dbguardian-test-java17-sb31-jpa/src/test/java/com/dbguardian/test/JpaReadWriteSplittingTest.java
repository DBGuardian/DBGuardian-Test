package com.dbguardian.test;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.repository.UserRepository;
import com.dbguardian.test.service.UserService;
import io.dbguardian.model.RoutingContext;
import io.dbguardian.spring.RoutingContextHolder;
import io.dbguardian.spring.aspect.JpaDataSourceAdvice;
import io.dbguardian.spring.aspect.JpaDataSourceAdvisor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class JpaReadWriteSplittingTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void usesJpaTransactionManager() {
        assertTrue(transactionManager instanceof JpaTransactionManager);
    }

    @Test
    void repositoryCrudWorksThroughJpaRoutingAdvisor() {
        String username = "jpa31-test-user-" + System.nanoTime();
        User user = new User();
        user.setUsername(username);
        user.setEmail("jpa31@test.com");

        User saved = userService.create(user);
        assertNotNull(saved.getId());
        assertTrue(userService.findById(saved.getId()).isPresent());
        assertTrue(userService.listUsers().stream().anyMatch(item -> saved.getId().equals(item.getId())));
        assertTrue(userService.findByUsername(username).isPresent());
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void jpaAdvisorMatchesRepositoryAndClassifiesReadWriteMethods() throws Throwable {
        JpaDataSourceAdvisor advisor = new JpaDataSourceAdvisor();
        Method findById = UserRepository.class.getMethod("findById", Object.class);
        Method save = UserRepository.class.getMethod("save", Object.class);

        assertTrue(advisor.getPointcut().getMethodMatcher().matches(findById, UserRepository.class));
        assertTrue(advisor.getPointcut().getMethodMatcher().matches(save, UserRepository.class));

        assertRoutingContext(findById, "read");
        assertRoutingContext(save, "write");
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void repositoryIsInjected() {
        assertNotNull(userRepository);
    }

    private void assertRoutingContext(Method method, String operation) throws Throwable {
        new JpaDataSourceAdvice().invoke(new MethodInvocation() {
            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public Object proceed() {
                RoutingContext context = RoutingContextHolder.get();
                assertNotNull(context);
                assertEquals("jpa", context.getOrmType());
                assertEquals(operation, context.getOperation());
                return null;
            }

            @Override
            public Object getThis() {
                return userRepository;
            }

            @Override
            public AccessibleObject getStaticPart() {
                return method;
            }
        });
    }
}