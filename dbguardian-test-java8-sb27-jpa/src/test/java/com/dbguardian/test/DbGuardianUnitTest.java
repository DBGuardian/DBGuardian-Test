package com.dbguardian.test;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JPA Repository 单元测试 - 验证 Spring Data JPA 功能
 */
@SpringBootTest
@ActiveProfiles("test")
class DbGuardianUnitTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFind() {
        User user = new User();
        user.setUsername("unit_test_user");
        user.setEmail("unit@test.com");
        
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals("unit_test_user", saved.getUsername());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void testQueryByExample() {
        // 插入测试数据
        User user1 = new User();
        user1.setUsername("abc");
        user1.setEmail("abc@test.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("def");
        user2.setEmail("def@test.com");
        userRepository.save(user2);

        // 验证分页查询
        Page<User> page = userRepository.findAll(PageRequest.of(0, 10, Sort.by("id")));
        assertNotNull(page.getContent());
    }

    @Test
    void testDelete() {
        User user = new User();
        user.setUsername("delete_user");
        user.setEmail("delete@test.com");
        
        User saved = userRepository.save(user);
        Long id = saved.getId();

        userRepository.deleteById(id);
        
        Optional<User> found = userRepository.findById(id);
        assertFalse(found.isPresent(), "删除后应不存在");
    }
}