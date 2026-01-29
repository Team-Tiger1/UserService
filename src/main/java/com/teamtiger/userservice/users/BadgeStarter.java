package com.teamtiger.userservice.users;

import com.teamtiger.userservice.users.entities.Badge;
import com.teamtiger.userservice.users.entities.BadgeGrade;
import com.teamtiger.userservice.users.entities.BadgeName;
import com.teamtiger.userservice.users.repositories.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BadgeStarter implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    public void run(String... args) throws Exception {
//        if(badgeRepository.count() > 0) {
//            return;
//        }
//
//        Badge badge = Badge.builder()
//                .name(BadgeName.HOT_SHOPPER)
//                .grade(BadgeGrade.BRONZE)
//                .build();
//
//        Badge badge1 = Badge.builder()
//                .name(BadgeName.HOT_SHOPPER)
//                .grade(BadgeGrade.SILVER)
//                .build();
//
//        Badge badge2 = Badge.builder()
//                .name(BadgeName.HOT_SHOPPER)
//                .grade(BadgeGrade.GOLD)
//                .build();
//
//        badgeRepository.save(badge);
//        badgeRepository.save(badge1);
//        badgeRepository.save(badge2);
    }
}
