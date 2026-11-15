package com.dbguardian.test.service;

import com.dbguardian.test.model.User;
import com.dbguardian.test.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
}
