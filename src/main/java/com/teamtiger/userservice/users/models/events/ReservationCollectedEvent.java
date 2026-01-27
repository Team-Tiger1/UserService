package com.teamtiger.userservice.users.models.events;


import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationCollectedEvent(

        UUID userId,
        LocalDateTime reservationCollected
) {
}
