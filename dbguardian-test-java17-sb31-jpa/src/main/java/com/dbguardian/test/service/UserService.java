package com.dbguardian.test.service;

import com.dbguardian.test.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User create(User user);

    Optional<User> findById(Long id);

    List<User> listUsers();

    Optional<User> findByUsername(String username);

    void deleteById(Long id);
}