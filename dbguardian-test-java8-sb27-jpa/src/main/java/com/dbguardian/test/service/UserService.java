package com.dbguardian.test.service;

import com.dbguardian.test.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> listUsers();

    Optional<User> findByUsername(String username);

    void deleteById(Long id);
}