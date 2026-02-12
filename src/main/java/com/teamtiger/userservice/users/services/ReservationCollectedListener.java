package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.config.UserRabbitMQConfig;
import com.teamtiger.userservice.users.entities.Streak;
import com.teamtiger.userservice.users.models.events.ReservationCollectedEvent;
import com.teamtiger.userservice.users.repositories.StreakRepository;
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

    /**
     * Method for handling messages from the queue and updating the user streak
     * @param event The message from the queue
     */
    @RabbitListener(queues = UserRabbitMQConfig.QUEUE)
    public void handle(@NonNull ReservationCollectedEvent event) {

        //Extract data from message
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
            //If new week since last reservation
            int currentStreak = streak.getStreak();
            streak.setStreak(currentStreak + 1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);
        } else if (daysElapsed >= 14) {
            //If more than week has passed, reset the streak
            streak.setStreak(1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);

        }


    }

}
