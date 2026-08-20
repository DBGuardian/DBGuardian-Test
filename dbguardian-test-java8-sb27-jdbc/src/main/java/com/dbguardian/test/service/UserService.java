package com.dbguardian.test.service;

import com.dbguardian.test.model.User;
import com.dbguardian.test.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setCreateTime(user.getCreateTime() == null ? now : user.getCreateTime());
        user.setUpdateTime(now);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User create(User user) {
        return saveUser(user);
    }

    public Optional<User> get(long id) {
        return userRepository.findById(id);
    }

    public List<User> list() {
        return userRepository.findAll();
    }

    public boolean update(long id, User user) {
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.update(id, user);
    }

    public boolean delete(long id) {
        return userRepository.deleteById(id);
    }

    public String currentNode() {
        return userRepository.currentNode();
    }
}