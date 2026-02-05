package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.auth.models.Role;
import com.teamtiger.userservice.users.entities.Streak;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.exceptions.*;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.repositories.StreakRepository;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceJPA implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordHasher passwordHasher;
    private final UsernameGenerator usernameGenerator;
    private final StreakRepository streakRepository;

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

        //Get Refresh Token
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId(), Role.USER);

        return UserRegisterDTO.builder()
                .userDTO(UserMapper.toDTO(user))
                .refreshToken(refreshToken)
                .build();
    }

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

    @Override
    public UserDTO getUserProfile(String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        User savedUser = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public UserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO) {

        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        //If there are not new values return user entity
        if (updateUserDTO.getEmail() == null) {
            return UserMapper.toDTO(user);
        }

        //Update email
        String email = updateUserDTO.getEmail();
        if (email != null && !userRepository.existsByEmail(email)) {
            user.setEmail(email);
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public void updateUserPassword(String accessToken, UpdateUserPasswordDTO passwordDTO) {

        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean isOldPasswordCorrect = passwordHasher.matches(passwordDTO.getOldPassword(), user.getPassword());
        if (!isOldPasswordCorrect) {
            throw new PasswordIncorrectException();
        }

        String hashedPassword = passwordHasher.hashPassword(passwordDTO.getNewPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
    }

    @Override
    public StreakDTO getUserStreak(String accessToken) {
        UUID userId = jwtTokenUtil.getUuidFromToken(accessToken);
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("USER")) {
            throw new AuthorizationException();
        }

        Streak streak = streakRepository.findById(userId).orElseGet(() -> {
            return Streak.builder()
                    .streak(0)
                    .build();
        });


        if (streak.getLastReservation().isBefore(LocalDateTime.now().minusWeeks(1))) {
            //Reset streak
            streak.setStreak(0);
            streak = streakRepository.save(streak);
        }

        return new StreakDTO(streak.getStreak());

    }

    @Transactional
    @Override
    public void loadSeededUsers(String accessToken, List<UserSeedDTO> users) {
        String role = jwtTokenUtil.getRoleFromToken(accessToken);

        if (!role.equals("INTERNAL")) {
            throw new AuthorizationException();
        }

        List<User> entityList = users.stream()
                .map(dto -> User.builder()

                        .id(dto.getId())
                        .username(usernameGenerator.generateUsername())
                        .email(dto.getEmail())
                        .password(passwordHasher.hashPassword(dto.getPassword()))
                        .build())
                .toList();

        userRepository.saveAll(entityList);

        //Create streaks and save them
        List<Streak> streakList = users.stream()
                .filter(dto -> dto.getStreak() > 0)
                .map(dto -> {
                    return Streak.builder()
                            .userId(dto.getId())
                            .streak(dto.getStreak())
                            .lastReservation(dto.getLastReservationTime())
                            .build();
                })
                .toList();

        streakRepository.saveAll(streakList);
    }

    private static class UserMapper {
        public static UserDTO toDTO(User entity) {
            if (entity == null) return null;
            return UserDTO.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .email(entity.getEmail())
                    .build();
        }

        public static User toEntity(UserDTO dto) {
            if (dto == null) return null;
            return User.builder()
                    .id(dto.getId())
                    .username(dto.getUsername())
                    .email(dto.getEmail())
                    .build();
        }
    }


}
