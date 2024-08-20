package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.security.UserNotFoundException;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.types.mapper.UserMapper;
import com.writenbite.bisonfun.api.types.user.AuthorisedUserPayload;
import com.writenbite.bisonfun.api.types.user.UserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
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
    public UserPayload user(@Argument int id) {
        Optional<com.writenbite.bisonfun.api.database.entity.User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return null;
        }
        com.writenbite.bisonfun.api.database.entity.User userDb = optionalUser.get();
        return new UserPayload(userMapper.fromEntity(userDb));
    }

    @QueryMapping
    public UserPayload userByUsername(@Argument String username) {
        Optional<com.writenbite.bisonfun.api.database.entity.User> optionalUser = userService.getUserByUsername(username.toLowerCase());
        if (optionalUser.isEmpty()) {
            return null;
        }
        com.writenbite.bisonfun.api.database.entity.User userDb = optionalUser.get();
        return new UserPayload(userMapper.fromEntity(userDb));
    }

    @PreAuthorize("hasRole(T(com.writenbite.bisonfun.api.security.Role).ROLE_ACCESS)")
    @QueryMapping
    public AuthorisedUserPayload authorisedUser(Principal principal) throws UserNotFoundException {
        Optional<com.writenbite.bisonfun.api.database.entity.User> optionalUser = userService.getUserByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(principal.getName());
        }
        User user = optionalUser.get();
        return new AuthorisedUserPayload(userMapper.fromEntity(user), user.getEmail());
    }
}
