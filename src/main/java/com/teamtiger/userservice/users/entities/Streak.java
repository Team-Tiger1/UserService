package com.teamtiger.userservice.users.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "streaks")
public class Streak {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private Integer streak;

    private LocalDateTime lastReservation;


}
