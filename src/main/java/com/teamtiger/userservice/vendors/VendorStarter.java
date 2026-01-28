package com.teamtiger.userservice.vendors;

import com.teamtiger.userservice.auth.PasswordHasher;
import com.teamtiger.userservice.users.entities.User;
import com.teamtiger.userservice.users.repositories.UserRepository;
import com.teamtiger.userservice.users.services.UsernameGenerator;
import com.teamtiger.userservice.vendors.entities.Vendor;
import com.teamtiger.userservice.vendors.entities.VendorCategory;
import com.teamtiger.userservice.vendors.repositories.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VendorStarter implements CommandLineRunner {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordHasher passwordHasher;

    @Override
    public void run(String... args) throws Exception {

        Vendor vendor = Vendor.builder()
                .name("Exeter Bakery")
                .email("bakery@gmail.com")
                .password("password")
                .phoneNumber("534534534")
                .postcode("EX5 GEF")
                .category(VendorCategory.BAKERY)
                .streetAddress("35 Exeter Street")
                .build();

        User user = User.builder()
                .email("example@gmail.com")
                .password(passwordHasher.hashPassword("password"))
                .username(usernameGenerator.generateUsername())
                .build();

        userRepository.save(user);

        System.out.println(vendorRepository.save(vendor).getId());
    }
}
