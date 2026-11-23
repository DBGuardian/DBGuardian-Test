package com.dbguardian.test.service;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserMapper users;

    public UserService(UserMapper users) {
        this.users = users;
    }

    @Transactional
    public User create(User user) {
        users.insert(user);
        return users.selectById(user.getId()).orElseThrow(IllegalStateException::new);
    }

    public List<User> listUsers() {
        return users.selectAll();
    }

    public Optional<User> findById(Long id) {
        return users.selectById(id);
    }

    public Optional<User> findByUsername(String username) {
        return users.selectByUsername(username);
    }

    // A transaction binds the current writer connection before the mapper classifies the SELECT.
    @Transactional
    public Optional<User> findByIdFromMaster(Long id) {
        return users.selectById(id);
    }

    @Transactional
    public Optional<User> update(Long id, User user) {
        user.setId(id);
        users.update(user);
        return users.selectById(id);
    }

    @Transactional
    public boolean delete(Long id) {
        return users.deleteById(id) != 0;
    }

    public long readServerId() {
        return users.selectServerId();
    }

    @Transactional
    public long writeServerId() {
        return users.selectServerId();
    }
}
