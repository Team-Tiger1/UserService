//set JAVA_HOME=C:\Users\ivyfi\AppData\Local\Programs\Microsoft\jdk-17.0.17.10-hotspot
//.\mvnw.cmd test -Dtest=-----

package com.teamtiger.userservice.integration;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.teamtiger.userservice.users.entities.Streak;
import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.UserRegisterDTO;
import com.teamtiger.userservice.users.models.events.ReservationCollectedEvent;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import com.teamtiger.userservice.users.services.ReservationCollectedListener;
import com.teamtiger.userservice.users.services.UserServiceJPA;

import jakarta.transaction.Transactional;




@SpringBootTest(properties = {"jwt.secret=ZmFrZS1qd3Qtc2VjcmV0LWZvci10ZXN0cy0zMi1ieXRlcy1sb25nISE="})

// integration test for user and streak
@Transactional //ensures databse changes roll back
class UserServices_IT {

    @Autowired
    private UserServiceJPA userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StreakRepository streakRepository;

    @Autowired
    private ReservationCollectedListener reservationCollectedListener;

    private CreateUserDTO createUserDTO;

    @BeforeEach
    void setUp() {
        createUserDTO = CreateUserDTO.builder()
                .email("test@exeter.ac.uk")
                .password("password123")
                .build();
    }

    /**
     * create user
     * check streak
     * simulate reservation
     * check streak
     */
    @Test
    void createUser_ReservationEvent_Streak_IT() {
        UserRegisterDTO register = userService.createUser(createUserDTO);

        UUID userId = register.getUserDTO().getId();
        assertNotNull(userId);
        assertTrue(userRepository.existsById(userId));

        //streak does not exist
        assertFalse(streakRepository.existsById(userId));

        //simulate reservation
        LocalDateTime timeNow = LocalDateTime.now();
        reservationCollectedListener.handle(new ReservationCollectedEvent(userId, timeNow));

        //streak now exists
        Streak streak = streakRepository.findById(userId).orElseThrow();
        assertEquals(1, streak.getStreak());

        //ensure the last reservation time has been updates,
        assertEquals(timeNow, streak.getLastReservation());
    }


    /**
     * tests if when 2 reservations are made in the same day,
     * the streak does not increase twice
     */
    @Test
    void twoReservations_OnSameDay_Streak_IT(){
        //reuse UserRegistration_IT
        UserRegisterDTO register = userService.createUser(createUserDTO);

        UUID userId = register.getUserDTO().getId();
        assertNotNull(userId);
        assertTrue(userRepository.existsById(userId));

        assertFalse(streakRepository.existsById(userId));

        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime later = timeNow.plusHours(3);

        reservationCollectedListener.handle(new ReservationCollectedEvent(userId, timeNow));

        Streak streak = streakRepository.findById(userId).orElseThrow();
        assertEquals(1, streak.getStreak());
        assertEquals(timeNow, streak.getLastReservation());

        //reserve again, with later time
        reservationCollectedListener.handle(new ReservationCollectedEvent(userId, later));
        Streak secondStreak = streakRepository.findById(userId).orElseThrow();
        //streak should still be 1
        assertEquals(1, secondStreak.getStreak(),"streak shouldnt increase after each reservation");

//        //ensure the last reservation time has been updates,
//        assertEquals(later, secondStreak.getLastReservation());

    }




    //duplicate email
    //invalid email format
    //password min length
    //2 reservations on the same day don't increase streak twice

}
