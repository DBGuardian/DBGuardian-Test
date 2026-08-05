package com.dbguardian.test;

import com.dbguardian.annotation.ForceMaster;
import com.dbguardian.test.entity.User;
import com.dbguardian.test.repository.UserRepository;
import io.dbguardian.spring.RoutingContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 读写分离测试 - JPA 版本
 */
@SpringBootTest
@ActiveProfiles("test")
class ReadWriteSplittingTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * TC-001: 验证写操作（save）路由到主库
     */
    @Test
    void testWriteToMaster() {
        User user = new User();
        user.setUsername("jpa_test_user");
        user.setEmail("jpa@test.com");

        User saved = userRepository.save(user);
        assertNotNull(saved.getId(), "保存成功应返回ID");
        assertEquals("jpa_test_user", saved.getUsername());

        // 验证 context.ormType = "jpa"
        String ormType = RoutingContextHolder.get().getOrmType();
        assertEquals("jpa", ormType, "ORM类型应为 jpa");
    }

    /**
     * TC-002: 验证读操作（findById）路由到从库
     */
    @Test
    void testReadFromSlave() {
        // 先保存数据
        User user = new User();
        user.setUsername("jpa_read_user");
        user.setEmail("read@test.com");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent(), "应找到用户记录");
        assertEquals("jpa_read_user", found.get().getUsername());
    }

    /**
     * TC-003: 验证 @ForceMaster 注解强制走主库
     */
    @Test
    void testForceMasterAnnotation() {
        // 使用 @ForceMaster 的方法会强制路由到主库
        User user = new User();
        user.setUsername("jpa_force_master_user");
        user.setEmail("force@test.com");
        
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
}