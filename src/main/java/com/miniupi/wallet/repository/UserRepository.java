package com.miniupi.wallet.repository;

// This repository is mostly focused on authentication checks

import com.miniupi.wallet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {

    // Used during login to find the user by their username
    Optional<User> findByUsername(String username);

    // Used during registration to check if the username or email is already taken
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
