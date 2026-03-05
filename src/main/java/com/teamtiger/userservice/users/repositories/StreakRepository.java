package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface StreakRepository extends JpaRepository<Streak, UUID> {

    @Query("SELECT streak FROM Streak where userId = :userId")
    Integer getStreakForUser(UUID userId);

}
