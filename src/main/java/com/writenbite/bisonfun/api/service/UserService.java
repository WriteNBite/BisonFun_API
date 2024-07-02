package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.database.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepo;

    @Autowired
    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Optional<User> getUserByUsername(String username){
        log.info("Getting User {}", username);
        Optional<User> user = userRepo.getByUsername(username);
        log.debug("User: "+user);
        return user;
    }

    public Optional<User> getUserById(int id){
        log.info("Getting User by id: {}", id);
        Optional<User> user = userRepo.findById(id);
        log.debug("User: "+user);
        return user;
    }

    public User saveUser(User user){
        log.info("Saving User {}", user.getUsername());
        return userRepo.save(user);
    }

    public boolean existUserByUsernameOrEmail(String username, String email){
        log.info("Check existence of User by Username {} and Email {}", username, email);
        boolean exists = userRepo.existsByUsernameOrEmail(username, email);
        log.info("Existence of User with Username {} and Email {}: {}", username, email, exists);
        return exists;
    }
}
