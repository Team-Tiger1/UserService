package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.entities.badges.BadgeGrade;
import com.teamtiger.userservice.users.entities.badges.BadgeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeDTO {

    private BadgeName name;
    private BadgeGrade grade;
    private double currentAmount;
    private double threshold;

}
