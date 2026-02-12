package com.teamtiger.userservice.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Component responsible for hashing passwords
 */
@Component
public class PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hashes the plain text password into its hashed version
     * @param plainTextPassword The password the user or vendor has chosen
     * @return The hashed password
     */
    public String hashPassword(String plainTextPassword) {
        return encoder.encode(plainTextPassword);
    }

    /**
     * Compares the plain text password against a hashed one
     * @param plainTextPassword The plain text password the User or Vendor entered
     * @param hashedPassword The stored hashed password
     * @return True if passwords match, False if not
     */
    public boolean matches(String plainTextPassword, String hashedPassword) {
        return encoder.matches(plainTextPassword, hashedPassword);
    }
}
