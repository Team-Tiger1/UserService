package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT u.username, SUM(p.weight * bp.quantity) AS total_waste " +
            "FROM users u " +
            "JOIN reservation r ON u.user_id = r.user_id " +
            "JOIN bundle_products bp ON r.bundle_id = bp.bundle_id " +
            "JOIN products p ON bp.product_id = p.product_id " +
            "WHERE r.status = 'COLLECTED' " +
            "GROUP BY u.user_id, u.username " +
            "ORDER BY total_waste DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> countTopWasteSaved();

    @Query(value = "SELECT u.username, SUM(b.retail_price - b.price) AS total_money " +
            "FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "JOIN users u ON u.user_id = r.user_id " +
            "WHERE r.status = 'COLLECTED' " +
            "GROUP BY u.user_id, u.username " +
            "ORDER BY total_money DESC " +
            "LIMIT 10",
            nativeQuery = true)
    List<Object[]> countTopMoneySaved();

    @Query(value = "SELECT username, position, total_money FROM (" +
            "SELECT u.username, u.user_id, SUM(b.retail_price - b.price) AS total_money, " +
            "RANK() OVER (ORDER BY SUM(b.retail_price - b.price) DESC) AS position " +
            "FROM users u " +
            "JOIN reservation r ON r.user_id = u.user_id " +
            "JOIN bundles b ON b.bundle_id = r.bundle_id " +
            "WHERE r.status = 'COLLECTED' " +
            "GROUP BY u.user_id, u.username " +
            ") AS leaderboard WHERE user_id = :userId",
            nativeQuery = true)
    List<Object[]> findUserRankByMoneySaved(UUID userId);


    @Query(value = "SELECT username, position, totalWaste FROM ( " +
            "SELECT u.username, u.user_id, SUM(p.weight * bp.quantity) AS totalWaste, " +
            "RANK() OVER (ORDER BY SUM(p.weight * bp.quantity) DESC) AS position " +
            "FROM users u " +
            "JOIN reservation r ON u.user_id = r.user_id " +
            "JOIN bundle_products bp ON r.bundle_id = bp.bundle_id " +
            "JOIN products p ON bp.product_id = p.product_id " +
            "WHERE r.status = 'COLLECTED' " +
            "GROUP BY u.user_id, u.username" +
            ") AS leaderboard " +
            "WHERE user_id = :userId",
            nativeQuery = true)
    List<Object[]> findUserRankByWasteSaved(UUID userId);


    @Query(value = "SELECT EXISTS(SELECT 1 FROM reservation WHERE user_id = :userId AND status = 'COLLECTED')", nativeQuery = true)
    boolean doesUserHaveReservations(UUID userId);

}
