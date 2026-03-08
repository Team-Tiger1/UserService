package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {

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

    @Query(value = "SELECT SUM(b.retail_price - b.price) FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'",
    nativeQuery = true)
    Double countMoneySaved(UUID userId);

    @Query(value = "SELECT * FROM badges WHERE user_id = :userId", nativeQuery = true)
    Set<Badge> findAllByUserId(@Param("userId") String userId);





}
