package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
}
