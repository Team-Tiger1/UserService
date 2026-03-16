package com.teamtiger.userservice.users.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO for the GET streak endpoint
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StreakDTO {

    private Integer streak;

}
