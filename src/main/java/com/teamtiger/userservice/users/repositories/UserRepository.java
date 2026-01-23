package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


}
