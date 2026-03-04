package com.teamtiger.userservice.users.entities;

import java.util.Map;

public class BadgeValues {

    public static final Map<BadgeName, Double[]> BADGE_THRESHOLDS = Map.of(
            BadgeName.THE_EXPLORER,    new Double[]{3.0, 5.0, 10.0},
            BadgeName.LOYAL_SHOPPER,       new Double[]{5.0, 10.0, 20.0},
            BadgeName.HOT_SHOPPER, new Double[]{5.0, 15.0, 25.0},
            BadgeName.WASTE_KING,       new Double[]{5.0, 25.0, 100.0},
            BadgeName.CATEGORY_KING,    new Double[]{2.0, 4.0, 6.0},
            BadgeName.WEEKLY_WARRIOR,      new Double[]{2.0, 4.0, 8.0},
            BadgeName.WALLET_WATCHER,      new Double[]{10.0, 50.0, 100.0}
    );

}
