package com.teamtiger.userservice.users.services;

import com.teamtiger.userservice.users.models.*;

import java.util.List;

public interface UserService {

    UserRegisterDTO createUser(CreateUserDTO userDTO);

    UserRegisterDTO userLogin(LoginDTO loginDTO);

    UserDTO getUserProfile(String accessToken);

    UserDTO updateUserProfile(String accessToken, UpdateUserDTO updateUserDTO);

    void updateUserPassword(String accessToken, UpdateUserPasswordDTO passwordDTO);

    StreakDTO getUserStreak(String accessToken);

    void loadSeededUsers(String accessToken, List<UserSeedDTO> users);

    List<UserBadgeDTO> getAllBadgesForUser(String accessToken);

    void deleteUser(String accessToken);

    LeaderboardDTO getLeaderboard(String accessToken, LeaderboardOption option);
  
    DisputeDTO createDispute(String accessToken, CreateDisputeDTO createDisputeDTO);

    List<DisputeDTO> getDisputes(String accessToken);

    UserImpactDTO getUserImpact(String accessToken, String period);

}
