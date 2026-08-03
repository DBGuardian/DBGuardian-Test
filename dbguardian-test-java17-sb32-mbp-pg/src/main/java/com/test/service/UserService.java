package com.test.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.test.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    User saveUser(User user);
    User getUserById(Long id);
    List<User> listUsers();
}