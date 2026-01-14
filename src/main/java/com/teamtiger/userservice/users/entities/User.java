package com.teamtiger.userservice.users.entities;

import com.teamtiger.userservice.users.UserConstants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "varchar", length = UserConstants.MAX_USERNAME_LENGTH, nullable = false)
    private String username;

    @Column(columnDefinition = "varchar", nullable = false)
    private String email;

    @Column(columnDefinition = "varchar", nullable = false)
    private String password;

    @Version
    private Long version;

}
