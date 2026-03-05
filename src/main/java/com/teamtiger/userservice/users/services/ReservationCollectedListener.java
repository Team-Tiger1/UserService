package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.config.UserRabbitMQConfig;
import com.teamtiger.userservice.users.entities.*;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.models.events.ReservationCollectedEvent;
import com.teamtiger.userservice.users.repositories.BadgeRepository;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ReservationCollectedListener {

    private final StreakRepository streakRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    //Holds method reference for each badge
    private final Map<BadgeName, Function<UUID, ? extends Number>> badgeRepositoryLookup = Map.of(
            BadgeName.THE_EXPLORER, badgeRepository::countUniqueVendorReservations,
            BadgeName.LOYAL_SHOPPER, badgeRepository::countBundlesFromSameVendor,
            BadgeName.HOT_SHOPPER, badgeRepository::countTotalBundlesForUser,
            BadgeName.WASTE_KING, badgeRepository::countWasteSaved,
            BadgeName.CATEGORY_KING, badgeRepository::countDistinctUserCategories,
            BadgeName.WEEKLY_WARRIOR, streakRepository::getStreakForUser,
            BadgeName.WALLET_WATCHER, badgeRepository::countMoneySaved
    );



    /**
     * Method for handling messages from the queue and updating the user streak
     * @param event The message from the queue
     */
    @RabbitListener(queues = UserRabbitMQConfig.QUEUE)
    public void handle(@NonNull ReservationCollectedEvent event) {

        //Extract data from message
        UUID userId = event.userId();
        LocalDateTime collectedTime = event.reservationCollected();

        updateStreak(userId, collectedTime);
        updateBadges(userId);

    }

    /**
     * Updates the users badges when a bundle is collected
     * @param userId The users database Id
     */
    private void updateBadges(UUID userId) {

        //Function to calculate the badge grade based on thresholds
        BiFunction<BadgeName, Number, BadgeGrade> calculateBadgeGrade = (badgeName, input) -> {
          Double[] thresholds = BadgeValues.BADGE_THRESHOLDS.get(badgeName);
          double number = input.doubleValue();

          if(number < thresholds[0]) {
              return BadgeGrade.UNRANKED;
          }

          if(number < thresholds[1]) {
              return BadgeGrade.BRONZE;
          }

          if(number < thresholds[2]) {
              return BadgeGrade.SILVER;
          }

          return BadgeGrade.GOLD;
        };

        try {

            //Get badges for the user
            User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
            Set<Badge> badges = user.getBadges();

            for(Badge badge : badges) {

                //Get corresponding SQL method for badge
                Function<UUID, ? extends Number> sqlMethod = badgeRepositoryLookup.get(badge.getName());

                if(sqlMethod != null) {
                    //Run SQL query and calculate new badge grade
                    Number output = sqlMethod.apply(userId);
                    BadgeGrade grade = calculateBadgeGrade.apply(badge.getName(), output);
                    badge.setGrade(grade);
                }
            }

            badgeRepository.saveAll(badges);

        }

        catch (UserNotFoundException ignored) {}

    }

    /**
     * Updates user streak, depending on whether they've made at least one reservation every week
     * @param userId The users database Id
     * @param collectedTime The time the bundle was picked up
     */
    private void updateStreak(UUID userId, LocalDateTime collectedTime) {
        Streak streak = streakRepository.findById(userId).orElseGet(() -> streakRepository.save(
                Streak.builder()
                        .userId(userId)
                        .streak(1)
                        .lastReservation(collectedTime)
                        .build()));

        //Calculate whether a week has passed since the last reservation
        long daysElapsed = Duration.between(streak.getLastReservation(), collectedTime).toDays();

        if (daysElapsed >= 7 && daysElapsed < 14) {
            //If new week since last reservation
            int currentStreak = streak.getStreak();
            streak.setStreak(currentStreak + 1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);
        } else if (daysElapsed >= 14) {
            //If more than week has passed, reset the streak
            streak.setStreak(1);
            streak.setLastReservation(collectedTime);
            streakRepository.save(streak);
        }
    }

}
