package com.teamtiger.userservice.users.models;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {

    private List<LeaderboardEntry> top;

    private String username;
    private double value;
    private int position;

    @AllArgsConstructor
    @Getter
    public static class LeaderboardEntry {
        private String username;
        private double value;
    }

}
