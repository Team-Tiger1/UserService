package com.teamtiger.userservice.users.models;

import com.teamtiger.userservice.users.entities.BadgeGrade;
import com.teamtiger.userservice.users.entities.BadgeName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBadgeDTO {

    private BadgeName name;
    private BadgeGrade grade;
    private double amountLeft;

}
