package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.exceptions.UsernameAlreadyTakenException;
import com.teamtiger.userservice.users.models.*;
import com.teamtiger.userservice.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceJPA implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public CreatedUserDTO createUser(CreateUserDTO userDTO) {

        //Check if username is already taken
        String trimmedUsername = userDTO.getUsername().trim();
        if(userRepository.existsByUsername(trimmedUsername)) {
            throw new UsernameAlreadyTakenException();
        }

        //Hash Password
        String hashedPassword = PasswordHasher.hashPassword(userDTO.getPassword());

        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(hashedPassword)
                .build();

        //Save user entity to DB
        User savedUser = userRepository.save(user);

        //Get Refresh Token
        String refreshToken = jwtTokenUtil.generateRefreshToken(savedUser.getUsername(), "USER");


        CreatedUserDTO createdUserDTO = CreatedUserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .refreshToken(refreshToken)
                .build();

        return createdUserDTO;
    }

    @Override
    public CreatedUserDTO userLogin(LoginDTO loginDTO) {

        //Check if username matches record in DB
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(UserNotFoundException::new);

        //Check if password matches hashed version
        boolean doesPasswordMatch = PasswordHasher.matches(loginDTO.getPassword(), user.getPassword());
        if(!doesPasswordMatch) {
            throw new PasswordIncorrectException();
        }

        //Generate new refresh token
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername(), "USER");

        return CreatedUserDTO.builder()
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    @Override
    public UserDTO getUserProfile(String accessToken) {
        String username = jwtTokenUtil.getUsernameFromToken(accessToken);

        User savedUser = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        return UserMapper.toDTO(savedUser);
    }

    @Override
    public CreatedUserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO) {

        String username = jwtTokenUtil.getUsernameFromToken(accessToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        //If there are not new values return user entity
        if(updateUserDTO.getUsername() == null && updateUserDTO.getEmail() == null) {
            return CreatedUserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
        }

        //Update email
        if(updateUserDTO.getEmail() != null) {
            user.setEmail(updateUserDTO.getEmail());
        }

        //Update username and issue new refresh token
        String refreshToken = null;
        if(updateUserDTO.getUsername() != null) {
            user.setUsername(updateUserDTO.getUsername());

            refreshToken = jwtTokenUtil.generateRefreshToken(updateUserDTO.getUsername(), "USER");
        }

        User savedUser = userRepository.save(user);
        return CreatedUserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .refreshToken(refreshToken)
                .build();
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


    public static class PasswordHasher {
        private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        public static String hashPassword(String plainTextPassword) {
            return encoder.encode(plainTextPassword);
        }

        public static boolean matches(String plainTextPassword, String hashedPassword) {
            return encoder.matches(plainTextPassword, hashedPassword);
        }

    }
}
