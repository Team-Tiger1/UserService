package com.teamtiger.userservice.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenUtil {

    // Load secret from environment variables
    @Value("${JWT_SECRET}")
    private String key;

    // Access token expires in 15 minutes
    private static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000;

    // Refresh token expires in 7 days
    public static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000;


    public String generateAccessToken(String username) {

        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    public String generateRefreshToken(String username) {

        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(SignatureAlgorithm.HS256, hmacKey)
                .compact();
    }


    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}