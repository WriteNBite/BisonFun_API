package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.types.user.User;
import com.writenbite.bisonfun.api.types.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @QueryMapping
    public User user(@Argument int id) {
        Optional<com.writenbite.bisonfun.api.database.entity.User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return null;
        }
        com.writenbite.bisonfun.api.database.entity.User userDb = optionalUser.get();
        return userMapper.fromEntity(userDb);
    }

    @QueryMapping
    public User userByUsername(@Argument String username) {
        Optional<com.writenbite.bisonfun.api.database.entity.User> optionalUser = userService.getUserByUsername(username.toLowerCase());
        if (optionalUser.isEmpty()) {
            return null;
        }
        com.writenbite.bisonfun.api.database.entity.User userDb = optionalUser.get();
        return userMapper.fromEntity(userDb);
    }
}
