package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.database.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getUserByUsername(String username) {
        log.info("Getting User {}", username);
        return userRepo.getByUsername(username);
    }

    public Optional<User> getUserById(int id) {
        log.info("Getting User by id: {}", id);
        return userRepo.findById(id);
    }

    public User saveUser(User user) {
        log.info("Saving User {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public boolean existUserByUsernameOrEmail(String username, String email) {
        log.info("Check existence of User by Username {} and Email {}", username, email);
        boolean exists = userRepo.existsByUsernameOrEmail(username, email);
        log.info("Existence of User with Username {} and Email {}: {}", username, email, exists);
        return exists;
    }
}
