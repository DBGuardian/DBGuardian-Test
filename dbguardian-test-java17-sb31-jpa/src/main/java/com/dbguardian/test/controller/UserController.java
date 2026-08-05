package com.dbguardian.test.controller;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody User user) {
        return success(userService.create(user), "用户创建成功（已路由到主库）");
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        return userService.findById(id)
                .<Map<String, Object>>map(user -> success(user, "查询成功（已路由到从库）"))
                .orElseGet(() -> failure("用户不存在: " + id));
    }

    @GetMapping("/list")
    public Map<String, Object> list() {
        List<User> users = userService.listUsers();
        Map<String, Object> result = success(users, "查询成功（已路由到从库）");
        result.put("count", users.size());
        return result;
    }

    @GetMapping("/by-username/{username}")
    public Map<String, Object> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .<Map<String, Object>>map(user -> success(user, "查询成功（已强制路由到主库）"))
                .orElseGet(() -> failure("用户不存在: " + username));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return success(null, "用户已删除（已路由到主库）");
    }

    private Map<String, Object> success(Object data, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> failure(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}