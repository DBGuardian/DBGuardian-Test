package com.dbguardian.test.service;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User create(User user) {
        long id = users.insert(user);
        return users.findById(id).orElseThrow(() -> new IllegalStateException("Inserted user not found: " + id));
    }

    public List<User> listUsers() {
        return users.findAll();
    }

    public Optional<User> findById(Long id) {
        return users.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return users.findByUsername(username);
    }

    // A regular transaction binds reads to the active writer, even after promotion.
    @Transactional
    public Optional<User> findByIdFromMaster(Long id) {
        return users.findById(id);
    }

    @Transactional
    public Optional<User> update(Long id, User user) {
        users.update(id, user);
        return users.findById(id);
    }

    @Transactional
    public boolean delete(Long id) {
        return users.delete(id);
    }

    public long readServerId() {
        return users.serverId();
    }

    @Transactional
    public long writeServerId() {
        return users.serverId();
    }
}
