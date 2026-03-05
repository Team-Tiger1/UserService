package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.Badge;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {

    @SQL("SELECT COUNT(DISTINCT v.vendor_id) FROM vendor AS v " +
            "JOIN bundles b ON b.vendor_id = v.vendor_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE user_id = :userId AND r.status = 'COLLECTED'")
    Integer countUniqueVendorReservations(UUID userId);

    @SQL("SELECT MAX(bundle_count) FROM ( " +
            "SELECT COUNT(b.bundle_id) AS bundle_count " +
            "FROM bundles AS b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'" +
            "GROUP BY b.vendor_id) AS bundle_summary")
    Integer countBundlesFromSameVendor(UUID userId);

    @SQL("SELECT COUNT(b.bundle_id) FROM bundles as b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'")
    Integer countTotalBundlesForUser(UUID userId);

    @SQL("SELECT SUM(p.weight * bp.quantity) FROM products p " +
            "JOIN bundle_products bp ON bp.product_id = p.product_id " +
            "JOIN bundles b ON bp.bundle_id = b.bundle_id " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'")
    Double countWasteSaved(UUID userId);

    @SQL("SELECT COUNT(DISTINCT b.category) FROM bundles as b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'")
    Integer countDistinctUserCategories(UUID userId);

    @SQL("SELECT SUM(b.retail_price - b.price) FROM bundles b " +
            "JOIN reservation r ON r.bundle_id = b.bundle_id " +
            "WHERE r.user_id = :userId AND r.status = 'COLLECTED'")
    Double countMoneySaved(UUID userId);





}
