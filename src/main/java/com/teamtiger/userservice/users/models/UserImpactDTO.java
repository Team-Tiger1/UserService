package com.teamtiger.userservice.users.models;

import lombok.*;

/**
 * DTO containing user impact metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImpactDTO {
    private double moneySaved;
    private int wasteSaved;
    private int totalOrders;
}
