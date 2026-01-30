package com.teamtiger.userservice.auth;

import com.teamtiger.userservice.auth.models.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenUtil {

    // Load secret from environment variables
    @Value("${jwt.secret}")
    private String key;

    // Access token expires in 15 minutes
    private static final Duration ACCESS_TOKEN_EXPIRY = Duration.of(15, ChronoUnit.MINUTES);

    // Refresh token expires in 7 days
    public static final Duration REFRESH_TOKEN_EXPIRY = Duration.of(6, ChronoUnit.MONTHS);


    public String generateAccessToken(UUID uuid, Role role) {

        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        Claims claims = Jwts.claims();
        claims.setSubject(uuid.toString());
        claims.put("role", role.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY.toMillis()))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    public String generateRefreshToken(UUID uuid, Role role) {

        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        Claims claims = Jwts.claims().setSubject(uuid.toString());
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY.toMillis()))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    public UUID getUuidFromToken(String token) {
        return UUID.fromString(getClaimsFromToken(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    private Claims getClaimsFromToken(String token) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.parserBuilder()
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}