package com.teamtiger.userservice.users;

import com.teamtiger.userservice.users.entities.Badge;
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
        if(badgeRepository.count() > 0) {
            return;
        }

        Badge badge = Badge.builder()
                
                .build();
    }
}
