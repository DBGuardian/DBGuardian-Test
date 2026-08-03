package com.test.controller;

import com.test.entity.User;
import com.test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "PostgreSQL 读写分离测试")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "创建用户", description = "写操作路由到当前主库")
    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        return Map.of("success", true, "data", userService.saveUser(user));
    }

    @Operation(summary = "获取用户", description = "读操作路由到当前可读节点")
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", user != null);
        result.put("data", user);
        return result;
    }

    @Operation(summary = "获取用户列表", description = "读操作路由到当前可读节点")
    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.listUsers();
    }
}