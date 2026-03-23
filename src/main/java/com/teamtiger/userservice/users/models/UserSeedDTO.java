package com.teamtiger.userservice.users.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for data required when seeding generated users
 */
@RequiredArgsConstructor
@Getter
@Setter
@Schema(description = "Request body for seeding user data")
public class UserSeedDTO {

    @NotNull
    @Schema(description = "User ID")
    private UUID userId;

    @NotBlank
    @Email
    @Schema(description = "User email")
    private String email;

    @NotBlank
    @Schema(description = "User password")
    private String password;

    @NotNull
    @Schema(description = "Current streak")
    private Integer streak;

    @NotNull
    @Schema(description = "Last reservation time")
    private LocalDateTime lastReservationTime;

}
