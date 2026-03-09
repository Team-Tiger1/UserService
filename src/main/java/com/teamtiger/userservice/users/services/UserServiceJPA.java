package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.entities.*;
import com.teamtiger.userservice.users.entities.badges.Badge;
import com.teamtiger.userservice.users.entities.badges.BadgeGrade;
import com.teamtiger.userservice.users.entities.badges.BadgeName;
import com.teamtiger.userservice.users.entities.badges.BadgeValues;
import com.teamtiger.userservice.users.entities.disputes.Dispute;
import com.teamtiger.userservice.users.entities.disputes.DisputeStatus;
import com.teamtiger.userservice.users.exceptions.*;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.repositories.BadgeRepository;
import com.teamtiger.userservice.users.repositories.DisputeRepository;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
    private final DisputeRepository disputeRepository;

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

        //Save badges
        badgeRepository.saveAll(createAllUnrankedBadges(user.getId()));

        //Get Refresh Token
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), Role.USER);

        return UserRegisterDTO.builder()
                .userDTO(UserMapper.toDTO(user))
                .refreshToken(refreshToken)
                .build();
    }

    private Set<Badge> createAllUnrankedBadges(UUID userId) {
        Set<Badge> newBadges = new HashSet<>();
        for (BadgeName badgeName : BadgeValues.BADGE_THRESHOLDS.keySet()) {

            newBadges.add(Badge.builder()
                    .name(badgeName)
                    .grade(BadgeGrade.UNRANKED)
                    .currentAmount(0)
                    .userId(userId)
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

        Set<Badge> badges = badgeRepository.findAllByUserId(id);

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
     * Deletes user associated information
     * @param accessToken JWT Token
     */
    @Override
    public void deleteUser(String accessToken) {

        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        UUID id = jwtTokenUtil.getUuidFromToken(accessToken);

        streakRepository.deleteById(id);

        badgeRepository.deleteAllByUserId(id);

        userRepository.deleteById(id);

    }

    /**
     * Gets the leaderboard data for the user
     * @param accessToken The users access token
     * @param option MONEY or WASTE for different metrics
     * @return Top 10 users and your user's rank
     */
    @Override
    public LeaderboardDTO getLeaderboard(String accessToken, LeaderboardOption option) {  

        //Validate role
        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        UUID id = jwtTokenUtil.getUuidFromToken(accessToken);

        //Get Top 10 Users
        List<LeaderboardDTO.LeaderboardEntry> entries = getTopTenLeaderBoard(option);

        List<Object[]> currentUser;

        //Get associated data from database
        if(option == LeaderboardOption.WASTE) {
            currentUser = userRepository.findUserRankByWasteSaved(id);
        } else {
            currentUser = userRepository.findUserRankByMoneySaved(id);
        }

        String username = (String) currentUser.get(0)[0];
        int rank = ((Number) currentUser.get(0)[1]).intValue();
        double value = ((Number) currentUser.get(0)[2]).doubleValue();

        //Check if value is negative
        if(value < 0) {
            value = 0;
        }

        return LeaderboardDTO.builder()
                .top(entries)
                .position(rank)
                .username(username)
                .value(value)
      }

    /**
     * Validates dispute information, saves it and returns related dispute
     * @param accessToken User access token
     * @param createDisputeDTO Dispute information
     * @return Saved dispute
     */
    @Override
    public DisputeDTO createDispute(String accessToken, CreateDisputeDTO createDisputeDTO) {
        //Get User reference
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //Check if bundle exists
        boolean doesBundleExist = disputeRepository.doesBundleExist(createDisputeDTO.getBundleId());
        if(!doesBundleExist) {
            throw new RuntimeException();
        }

        //Find vendor and bundle names
        Map<String, Object> vendorDetails = disputeRepository.findVendorDetailsFromBundle(createDisputeDTO.getBundleId());

        String vendorName = (String) vendorDetails.get("name");
        UUID vendorId = (UUID) vendorDetails.get("vendor_id");

        String bundleName = disputeRepository.findBundleName(createDisputeDTO.getBundleId());


        //Creates and saves bundle
        Dispute dispute = Dispute.builder()
                .bundleId(createDisputeDTO.getBundleId())
                .vendorId(vendorId)
                .status(DisputeStatus.SUBMITTED)
                .reason(createDisputeDTO.getReason())
                .description(createDisputeDTO.getDescription())
                .user(user)
                .timeCreated(LocalDateTime.now())
                .build();

        Dispute savedDispute = disputeRepository.save(dispute);



        return DisputeDTO.builder()
                .vendorName(vendorName)
                .bundleName(bundleName)
                .status(savedDispute.getStatus())
                .description(savedDispute.getDescription())
                .reason(savedDispute.getReason())
                .timeCreated(savedDispute.getTimeCreated())
                .build();
    }
      

    /**
     * Calculates the top 10 users for money or waste saved
     * @param option Chooses between money and waste
     * @return List of entries
     */
    private List<LeaderboardDTO.LeaderboardEntry> getTopTenLeaderBoard(LeaderboardOption option) {
        List<Object[]> topUsers;

        //Get associated data from database
        if(option == LeaderboardOption.WASTE) {
            topUsers = userRepository.countTopWasteSaved();
        } else {
            topUsers = userRepository.countTopMoneySaved();
        }

        //Cast data to DTO
        List<LeaderboardDTO.LeaderboardEntry> entries = new ArrayList<>();
        for(Object[] topUser : topUsers) {
            String username = (String) topUser[0];
            double value = (double) topUser[1];
            entries.add(new LeaderboardDTO.LeaderboardEntry(username, value));
        }

        return entries;
    }

    /**
     * Gets all associated disputes for a user
     * @param accessToken The users access token
     * @return A list of DisputeDTO's
     */
    @Override
    public List<DisputeDTO> getDisputes(String accessToken) {

        //Validate role
        String role = jwtTokenUtil.getRoleFromToken(accessToken);
        if(!role.equals("USER")) {
            throw new AuthorizationException();
        }

        //Get User reference
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //Map Disputes to DTO
        return user.getDisputes().stream()
                .map(entity -> {

                            Map<String, Object> vendorDetails = disputeRepository.findVendorDetailsFromBundle(entity.getBundleId());
                            String vendorName = (String) vendorDetails.get("name");

                            return DisputeDTO.builder()
                                    .bundleName(disputeRepository.findBundleName(entity.getBundleId()))
                                    .vendorName(vendorName)
                                    .status(entity.getStatus())
                                    .reason(entity.getReason())
                                    .timeCreated(entity.getTimeCreated())
                                    .description(entity.getDescription())
                                    .build();

                        }
                ).toList();
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
