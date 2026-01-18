package com.teamtiger.userservice.vendors.repositories;

import com.teamtiger.userservice.vendors.entities.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    boolean existsByName(String name);

    Optional<Vendor> findByEmail(String email);

}
