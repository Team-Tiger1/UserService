package com.teamtiger.userservice.users.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Builder
@Data
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
