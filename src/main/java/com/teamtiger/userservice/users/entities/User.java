package com.teamtiger.userservice.users.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email", unique = true),
                @Index(name = "idx_username", columnList = "username", unique = true)
        }

)
public class User {

    @Id
//    @GeneratedValue(generator = "UUID")
//    @UuidGenerator
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_badges",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    private Set<Badge> badges;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private Streak streak;

    @Version
    private Long version;


}
