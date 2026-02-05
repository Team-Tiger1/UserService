package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.config.UserRabbitMQConfig;
import com.teamtiger.userservice.users.entities.Streak;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.models.events.ReservationCollectedEvent;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationCollectedListener {

    private final StreakRepository streakRepository;

    @RabbitListener(queues = UserRabbitMQConfig.QUEUE)
    public void handle(@NonNull ReservationCollectedEvent event) {
        //Logic for updating user streaks

        UUID userId = event.userId();
        LocalDateTime collectedTime = event.reservationCollected();

        Streak streak = streakRepository.findById(userId).orElseGet(() -> streakRepository.save(
                Streak.builder()
                        .userId(userId)
                        .streak(1)
                        .lastReservation(collectedTime)
                        .build()));

        //Calculate whether a week has passed since the last reservation
        long daysElapsed = Duration.between(streak.getLastReservation(), collectedTime).toDays();

        if (daysElapsed >= 7 && daysElapsed < 14) {
            int currentStreak = streak.getStreak();
            streak.setStreak(currentStreak + 1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);
        } else if (daysElapsed >= 14) {
            //Reset the streak
            streak.setStreak(1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);

        }


    }

}
