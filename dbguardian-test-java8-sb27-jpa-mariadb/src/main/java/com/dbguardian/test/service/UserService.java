package com.dbguardian.test.service;

import java.util.List;
import java.util.Optional;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.repository.UserRepository;
import io.dbguardian.annotation.ForceMaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User create(User user) {
        return users.save(user);
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

    @ForceMaster
    @Transactional
    public Optional<User> findByIdFromMaster(Long id) {
        return users.findById(id);
    }

    @Transactional
    public Optional<User> update(Long id, User input) {
        return users.findById(id).map(user -> {
            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            user.setPhone(input.getPhone());
            user.setStatus(input.getStatus());
            return user;
        });
    }

    @Transactional
    public void delete(Long id) {
        users.deleteById(id);
    }

    public long readServerId() {
        return users.findServerId().longValue();
    }

    @ForceMaster
    @Transactional
    public long writeServerId() {
        return users.findServerId().longValue();
    }
}
