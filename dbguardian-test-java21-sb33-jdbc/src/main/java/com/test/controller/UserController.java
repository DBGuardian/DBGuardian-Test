package com.test.controller;

import com.test.model.User;
import com.test.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping("/list")
    public List<User> listUsers() { return userService.listUsers(); }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) { return userService.getUserById(id); }

    @PostMapping
    public User createUser(@RequestBody User user) { return userService.saveUser(user); }

    @PutMapping("/{id}")
    public boolean updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.updateById(user);
    }

    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) { return userService.removeById(id); }
}