package com.teamtiger.userservice.vendors.repositories;

import com.teamtiger.userservice.vendors.entities.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    boolean existsByName(String name);

    Optional<Vendor> findByEmail(String email);

    @Modifying
    @Query(value = "DELETE FROM products AS p WHERE p.vendor_id = :vendorId", nativeQuery = true)
    void deleteAllVendorProducts(UUID vendorId);


}
