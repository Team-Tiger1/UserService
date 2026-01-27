package com.teamtiger.userservice.users.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private BadgeName name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private BadgeGrade grade;


}
