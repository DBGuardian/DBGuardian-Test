package com.dbguardian.test.controller;

import com.dbguardian.test.model.User;
import com.dbguardian.test.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", savedUser);
        result.put("message", "用户创建成功（已路由到主库）");
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", user != null);
        result.put("data", user);
        result.put("message", user != null ? "查询成功（已路由到从库）" : "用户不存在");
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
}