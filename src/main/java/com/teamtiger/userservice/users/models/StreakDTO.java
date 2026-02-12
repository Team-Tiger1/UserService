package com.teamtiger.userservice.users.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for the GET streak endpoint
 */
@Builder
@AllArgsConstructor
@Getter
public class StreakDTO {

    private Integer streak;

}
