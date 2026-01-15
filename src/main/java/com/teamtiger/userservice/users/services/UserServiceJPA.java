package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.auth.JwtTokenUtil;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.exceptions.PasswordIncorrectException;
import com.teamtiger.userservice.users.exceptions.UserNotFoundException;
import com.teamtiger.userservice.users.exceptions.UsernameAlreadyTakenException;
import com.teamtiger.userservice.users.models.CreateUserDTO;
import com.teamtiger.userservice.users.models.CreatedUserDTO;
import com.teamtiger.userservice.users.models.LoginDTO;
import com.teamtiger.userservice.users.models.UserDTO;
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
        String refreshToken = jwtTokenUtil.generateRefreshToken(savedUser.getUsername());


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
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getUsername());

        return CreatedUserDTO.builder()
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .username(user.getUsername())
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
