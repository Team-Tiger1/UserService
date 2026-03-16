package com.teamtiger.userservice.users.entities;

import com.teamtiger.userservice.users.entities.disputes.Dispute;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * Spring Entity that specifies the users database table
 * Specifies an index on the email to speed up query performance
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email", unique = true),
        }

)
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Dispute> disputes = new HashSet<>();

    @Version
    private Long version;


}
