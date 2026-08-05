package com.dbguardian.test.controller;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户控制器 - JPA CRUD API
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody User user) {
        User saved = userService.save(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", saved);
        result.put("message", "User created successfully");
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        Map<String, Object> result = new HashMap<>();
        if (user.isPresent()) {
            result.put("success", true);
            result.put("data", user.get());
            result.put("message", "User found");
        } else {
            result.put("success", false);
            result.put("message", "User not found: " + id);
        }
        return result;
    }

    @GetMapping("/list")
    public Map<String, Object> listUsers() {
        List<User> users = userService.listUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users);
        result.put("count", users.size());
        result.put("message", "查询成功（已路由到从库）");
        return result;
    }

    @GetMapping("/by-username/{username}")
    public Map<String, Object> findByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        Map<String, Object> result = new HashMap<>();
        if (user.isPresent()) {
            result.put("success", true);
            result.put("data", user.get());
            result.put("message", "User found");
        } else {
            result.put("success", false);
            result.put("message", "User not found: " + username);
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        userService.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "User deleted: " + id);
        return result;
    }
}