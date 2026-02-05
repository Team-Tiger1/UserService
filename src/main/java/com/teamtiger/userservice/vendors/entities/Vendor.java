package com.teamtiger.userservice.vendors.entities;

import com.teamtiger.userservice.vendors.VendorConstants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "vendor")
public class Vendor {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "vendor_id", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "varchar", length = VendorConstants.COMPANY_NAME_MAX_LENGTH, nullable = false)
    private String name;

    @Column(columnDefinition = "varchar", nullable = false)
    private String email;

    @Column(columnDefinition = "varchar", nullable = false, length = 10)
    private String phoneNumber;

    @Column(columnDefinition = "varchar", nullable = false)
    private String streetAddress;

    @Column(columnDefinition = "varchar", nullable = false, length = 7)
    private String postcode;

    @Column(columnDefinition = "varchar", length = VendorConstants.COMPANY_DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(columnDefinition = "varchar", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private VendorCategory category;

    @Version
    private Long version;

}