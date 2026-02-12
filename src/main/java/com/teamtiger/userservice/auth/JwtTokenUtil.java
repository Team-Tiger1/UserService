package com.teamtiger.userservice.auth;

import com.teamtiger.userservice.auth.models.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Spring Boot Component that handles generation and signing of JWT tokens
 */
@Component
public class JwtTokenUtil {

    // Load secret from environment variables
    @Value("${jwt.secret}")
    private String key;

    // Access token expires in 15 minutes
    private static final Duration ACCESS_TOKEN_EXPIRY = Duration.of(15, ChronoUnit.MINUTES);

    // Refresh token expires in ~6 months
    public static final Duration REFRESH_TOKEN_EXPIRY = Duration.of(180, ChronoUnit.DAYS);

    private Key hmacKey;

    /**
     * On startup, load and decode the secret key into memory
     */
    @PostConstruct
    private void init() {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        this.hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Method responsible for generating new access tokens
     * The UUID and Role is embedded in the payload
     * @param uuid The Id of the user or vendor (from the database)
     * @param role The type of client (USER or VENDOR)
     * @return A string of the new access token
     */
    public String generateAccessToken(UUID uuid, Role role) {

        Claims claims = Jwts.claims();
        claims.setSubject(uuid.toString());
        claims.put("role", role.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY.toMillis()))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    /**
     * Method responsible for generating new refresh tokens
     * The UUID and Role is embedded in the payload
     * @param uuid The Id of the user or vendor (from the database)
     * @param role The type of client (USER or VENDOR)
     * @return A string of the new refresh token
     */
    public String generateRefreshToken(UUID uuid, Role role) {

        Claims claims = Jwts.claims().setSubject(uuid.toString());
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY.toMillis()))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    /**
     * Extracts the UUID from the token payload
     * @param token The access or refresh token
     * @return The UUID of the User or Vendor
     */
    public UUID getUuidFromToken(String token) {
        return UUID.fromString(getClaimsFromToken(token).getSubject());
    }

    /**
     * Extracts the Role from the token payload
     * @param token The access or refresh token
     * @return The client Role type (String)
     */
    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    /**
     * Extract the claims from the token payload
     * @param token The access or refresh token
     * @return A Claims object that contains information about the client
     */
    private Claims getClaimsFromToken(String token) {

        return Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60) //Allows 60 seconds after expiry
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}