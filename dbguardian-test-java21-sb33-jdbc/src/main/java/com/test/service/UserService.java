package com.test.service;

import com.test.model.User;
import com.test.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) { this.repository = repository; }

    public User saveUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setCreateTime(user.getCreateTime() == null ? now : user.getCreateTime());
        user.setUpdateTime(now);
        return repository.save(user);
    }

    public User getUserById(Long id) { return repository.findById(id).orElse(null); }
    public List<User> listUsers() { return repository.findAll(); }

    public boolean updateById(User user) {
        user.setStatus(user.getStatus() == null ? 1 : user.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        return repository.update(user.getId(), user);
    }

    public boolean removeById(Long id) { return repository.deleteById(id); }
}