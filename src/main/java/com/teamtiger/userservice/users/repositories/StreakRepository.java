package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.Streak;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StreakRepository extends JpaRepository<Streak, UUID> {

    @SQL("SELECT streak FROM streaks where user_id = :userId")
    Integer getStreakForUser(UUID userId);

}
