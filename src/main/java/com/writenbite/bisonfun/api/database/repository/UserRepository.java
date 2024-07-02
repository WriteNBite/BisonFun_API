package com.writenbite.bisonfun.api.database.repository;

import com.writenbite.bisonfun.api.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> getByUsername(String username);

    Optional<User> getByEmail(String email);

    boolean existsByUsernameOrEmail(String username, String email);
}