package com.dbguardian.test.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping("/{id}")
    public Optional<User> getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/list")
    public List<User> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/by-username/{username}")
    public Optional<User> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping("/master/{id}")
    public Optional<User> getByIdFromMaster(@PathVariable Long id) {
        return userService.findByIdFromMaster(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @GetMapping("/routing")
    public Map<String, String> routingContract() {
        Map<String, String> contract = new LinkedHashMap<String, String>();
        contract.put("read", "slave");
        contract.put("write", "master");
        contract.put("transaction", "master");
        contract.put("forceMaster", "master");
        return contract;
    }
}