package com.dbguardian.test.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return findById(user.getId()).orElseThrow(IllegalStateException::new);
    }

    public List<User> listUsers() {
        return users.selectList(Wrappers.<User>lambdaQuery().orderByDesc(User::getId));
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.selectById(id));
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)));
    }

    // A regular transaction binds reads to the active writer, even after promotion.
    @Transactional
    public Optional<User> findByIdFromMaster(Long id) {
        return findById(id);
    }

    @Transactional
    public Optional<User> update(Long id, User user) {
        users.update(null, Wrappers.<User>lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getUsername, user.getUsername())
                .set(User::getEmail, user.getEmail())
                .set(User::getPhone, user.getPhone())
                .set(User::getStatus, user.getStatus()));
        return findById(id);
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
