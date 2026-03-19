package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.badges.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    @Query(value = "SELECT COUNT(DISTINCT v.vendor_id) FROM vendor AS v " +
            "JOIN bundles b ON b.vendor_id = v.vendor_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Integer countUniqueVendorReservations(UUID userId);

    @Query(value = "SELECT MAX(bundle_count) FROM ( " +
            "SELECT COUNT(b.bundle_id) AS bundle_count " +
            "FROM bundles AS b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'" +
            "GROUP BY b.vendor_id) AS bundle_summary",
    nativeQuery = true)
    Integer countBundlesFromSameVendor(UUID userId);

    @Query(value = "SELECT COUNT(b.bundle_id) FROM bundles as b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Integer countTotalBundlesForUser(UUID userId);

    @Query(value = "SELECT SUM(p.weight * bp.quantity) FROM products p " +
            "JOIN bundle_products bp ON bp.product_id = p.product_id " +
            "JOIN bundles b ON bp.bundle_id = b.bundle_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Double countWasteSaved(UUID userId);

    @Query(value = "SELECT COUNT(DISTINCT b.category) FROM bundles as b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Integer countDistinctUserCategories(UUID userId);

    @Query(value = "SELECT SUM(GREATEST(b.retail_price - b.price,0)) FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Double countMoneySaved(UUID userId);


    @Query(value = "SELECT SUM(GREATEST(b.retail_price - b.price,0)) FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED' " +
            "AND r.time_collected >= :startPeriod",
            nativeQuery = true)
    Double countMoneySavedForTimePeriod(UUID userId, LocalDateTime startPeriod);

    @Query(value = "SELECT SUM(p.weight * bp.quantity) FROM products p " +
            "JOIN bundle_products bp ON bp.product_id = p.product_id " +
            "JOIN bundles b ON bp.bundle_id = b.bundle_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED' " +
            "AND r.time_collected >= :startPeriod",
            nativeQuery = true)
    Double countWasteSavedForTimePeriod(UUID userId, LocalDateTime startPeriod);

    @Query(value = "SELECT COUNT(b.bundle_id) FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED' " +
            "AND r.time_collected >= :startPeriod",
            nativeQuery = true)
    Long countTotalOrdersForPeriod(UUID userId, LocalDateTime startPeriod);




    Set<Badge> findAllByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);





}
