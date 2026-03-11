package com.teamtiger.userservice.users.repositories;

import com.teamtiger.userservice.users.entities.disputes.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {


    @Query(value = "SELECT COUNT(b) > 0 FROM bundles AS b WHERE b.bundle_id = :bundleId", nativeQuery = true)
    boolean doesBundleExist(UUID bundleId);

    @Query(value = "SELECT v.name, v.vendor_id FROM vendor AS v " +
            "JOIN bundles b ON b.vendor_id = v.vendor_id " +
            "WHERE b.bundle_id = :bundleId", nativeQuery = true)
    Map<String, Object> findVendorDetailsFromBundle(UUID bundleId);

    @Query(value = "SELECT b.name FROM bundles AS b WHERE b.bundle_id = :bundleId", nativeQuery = true)
    String findBundleName(UUID bundleId);

    @Query(value = "SELECT d FROM disputes AS d " +
            "JOIN bundles b ON b.bundle_id = d.bundle_id " +
            "WHERE b.vendor_id = :vendorId " +
            "ORDER BY d.status, d.timeCreated", nativeQuery = true)
    Set<Dispute> findAllDisputesByVendor(UUID vendorId);




}
