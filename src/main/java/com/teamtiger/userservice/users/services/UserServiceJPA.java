package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.entities.*;
import com.teamtiger.userservice.users.exceptions.*;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.repositories.BadgeRepository;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class UserServiceJPA implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordHasher passwordHasher;
    private final UsernameGenerator usernameGenerator;
    private final StreakRepository streakRepository;
    private final BadgeRepository badgeRepository;

    /**
     * Creates a new user and stores the record on the database
     * @param userDTO A valid request body with the information for the user account
     * @return A UserRegisterDTO that has the user information and refresh token
     */
    @Override
    public UserRegisterDTO createUser(CreateUserDTO userDTO) {

        String username = usernameGenerator.generateUsername();

        //Check if email is already taken
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyTakenException();
        }

        //Hash Password
        String hashedPassword = passwordHasher.hashPassword(userDTO.getPassword());

        User user = User.builder()
                .username(username)
                .email(userDTO.getEmail())
                .password(hashedPassword)
                .badges(createAllUnrankedBadges())
                .build();

        //Save user entity to DB
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            //Try and generate new username if collision happens
            username = usernameGenerator.generateUsername();
            user.setUsername(username);
            user = userRepository.save(user);
        }

        //Get Refresh Token
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), Role.USER);

        return UserRegisterDTO.builder()
                .userDTO(UserMapper.toDTO(user))
                .refreshToken(refreshToken)
                .build();
    }

    private Set<Badge> createAllUnrankedBadges() {
        Set<Badge> newBadges = new HashSet<>();
        for (BadgeName badgeName : BadgeValues.BADGE_THRESHOLDS.keySet()) {

            newBadges.add(Badge.builder()
                    .name(badgeName)
                    .grade(BadgeGrade.UNRANKED)
                    .currentAmount(0)
                    .build());

        }
        return newBadges;
    }

    /**
     * Allows a user to login, and checks a users details against the database
     * @param loginDTO A valid login request body
     * @return A UserRegisterDTO that has the User record and refresh token
     */
    @Override
    public UserRegisterDTO userLogin(LoginDTO loginDTO) {

        //Check if email matches record in DB
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(UserNotFoundException::new);

        //Check if password matches hashed version
        boolean doesPasswordMatch = passwordHasher.matches(loginDTO.getPassword(), user.getPassword());
        if (!doesPasswordMatch) {
            throw new PasswordIncorrectException();
        }

        //Generate new refresh token
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), Role.USER);

        return UserRegisterDTO.builder()
                .refreshToken(refreshToken)
                .userDTO(UserMapper.toDTO(user))
                .build();
    }

    /**
     * Gets the user's details from the database
     * @param accessToken An access token (has userId in the payload)
     * @return User details from the database
     */
    @Override
    public UserDTO getUserProfile(String accessToken) {

        //Extract userId and query database
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        User savedUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserMapper.toDTO(savedUser);
    }

    /**
     * Updates a users database record with the details provided
     * @param accessToken An access token (has userId in the payload)
     * @param updateUserDTO Has the details that are being updated
     * @return The new user details after they've been updated
     */
    @Override
    public UserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }

        //Extract Id and query database
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //If there are not new values return user entity
        if (updateUserDTO.getEmail() == null) {
            return UserMapper.toDTO(user);
        }

        //Update email
        String email = updateUserDTO.getEmail();
        if (!userRepository.existsByEmail(email)) {
            user.setEmail(email);
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toDTO(savedUser);
    }

    /**
     * Updates a user's password, given that their old one is correct
     * @param accessToken An access token (has userId in the payload)
     * @param passwordDTO The new password and old password
     */
    @Override
    public void updateUserPassword(String accessToken, UpdateUserPasswordDTO passwordDTO) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }

        //Extract Id and query database
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //Check old password matches stored one
        boolean isOldPasswordCorrect = passwordHasher.matches(passwordDTO.getOldPassword(), user.getPassword());
        if (!isOldPasswordCorrect) {
            throw new PasswordIncorrectException();
        }

        String hashedPassword = passwordHasher.hashPassword(passwordDTO.getNewPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
    }

    /**
     * Gets a users streak stored on the database and generates one if they don't have one
     * @param accessToken An access token (has userId in the payload)
     * @return The streak wrapped in a DTO
     */
    @Override
    public StreakDTO getUserStreak(String accessToken) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }

        //Extract userId and query database
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        Streak streak = streakRepository.findById(userId).orElseGet(() -> Streak.builder()
                .streak(0)
                .build());

        if(streak.getLastReservation() == null) {
            return new StreakDTO(streak.getStreak());
        }


        if (streak.getLastReservation().isBefore(LocalDateTime.now().minusWeeks(1))) {
            //Reset streak if last reservation was longer than a week ago
            streak.setStreak(0);
            streak = streakRepository.save(streak);
        }

        return new StreakDTO(streak.getStreak());

    }

    /**
     * Saves seeded users to the database and generates references for streaks
     * @param accessToken An access token (has userId in the payload)
     * @param users List of generated users
     */
    @Transactional
    @Override
    public void loadSeededUsers(String accessToken, List<UserSeedDTO> users) {

        //Check role is valid
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        //Convert DTOs to Entities
        List<User> entityList = users.stream()
                .map(dto -> User.builder()
                        .id(dto.getUserId())
                        .username(usernameGenerator.generateUsername())
                        .email(dto.getEmail())
                        .password(passwordHasher.hashPassword(dto.getPassword()))
                        .build())
                .toList();


        userRepository.saveAll(entityList);


        //Create streaks and save them
        List<Streak> streakList = users.stream()
                .filter(dto -> dto.getStreak() > 0)
                .map(dto -> Streak.builder()
                        .userId(dto.getUserId())
                        .streak(dto.getStreak())
                        .lastReservation(dto.getLastReservationTime())
                        .build())
                .toList();


        streakRepository.saveAll(streakList);
    }

    @Override
    public List<UserBadgeDTO> getAllBadgesForUser(String accessToken) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        UUID id = jwtTokenUtil.getUuidFromToken(accessToken);

        Set<Badge> badges = badgeRepository.findAllByUser_Id(id);
        System.out.println(badges.size());

        BiFunction<BadgeName, BadgeGrade, Number> findNextThreshold = (badgeName, badgeGrade) -> {
                Double[] thresholds = BadgeValues.BADGE_THRESHOLDS.get(badgeName);
                if(badgeGrade == BadgeGrade.UNRANKED) {
                    return thresholds[0];
                }

                if(badgeGrade == BadgeGrade.BRONZE) {
                    return thresholds[1];
                }

                if(badgeGrade == BadgeGrade.SILVER) {
                    return thresholds[2];
                }

                return null;
        };

        return badges.stream()
                .map(entity -> {
                    Number threshold = findNextThreshold.apply(entity.getName(), entity.getGrade());

                    double safeThreshold = (threshold != null) ? threshold.doubleValue() : 0.0;

                    return UserBadgeDTO.builder()
                            .name(entity.getName())
                            .grade(entity.getGrade())
                            .currentAmount(entity.getCurrentAmount())
                            .threshold(safeThreshold)
                            .build();
                })
                .toList();
    }

    /**
     * Maps database entities to DTOs
     */
    private static class UserMapper {
        public static UserDTO toDTO(User entity) {
            if (entity == null) return null;
            return UserDTO.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .email(entity.getEmail())
                    .build();
        }
    }


}
