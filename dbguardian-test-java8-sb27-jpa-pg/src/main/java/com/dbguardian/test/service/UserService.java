package com.dbguardian.test.service;

import java.util.List;
import java.util.Optional;

import com.dbguardian.test.entity.User;

public interface UserService {

    User create(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByIdFromMaster(Long id);

    List<User> listUsers();

    void deleteById(Long id);
}