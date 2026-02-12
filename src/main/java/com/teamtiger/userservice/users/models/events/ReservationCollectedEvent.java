package com.teamtiger.userservice.users.models.events;


import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Record used to receive data from the RabbitMQ queue
 */
public record ReservationCollectedEvent(

        UUID userId,
        LocalDateTime reservationCollected
) {
}
