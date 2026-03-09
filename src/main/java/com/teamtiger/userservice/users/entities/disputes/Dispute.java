package com.teamtiger.userservice.users.entities.disputes;

import com.teamtiger.userservice.users.entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "dispute_id", updatable = false, nullable = false)
    private UUID id;

    @Column(updatable = false, nullable = false)
    private UUID bundleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private DisputeStatus status;

    @Enumerated(EnumType.STRING)
    private DisputeReason reason;

    @Column(updatable = false, nullable = false)
    private String description;

    @Column(updatable = false)
    private String vendorResponse;

    @Column(updatable = false)
    private LocalDateTime timeCreated;

}
