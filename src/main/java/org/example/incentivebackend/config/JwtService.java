package org.example.incentivebackend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Creates the signing key from the Base64 encoded secret.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate JWT token for a user.
     */
    public String generateToken(UserEntity user) {

        Date issuedAt = new Date();

        Date expirationDate =
                new Date(issuedAt.getTime() + expiration);

        return Jwts.builder()
                .subject(user.getUsername())

                // User ID is required later for audit logs
                .claim("userId", user.getUserId())

                .claim("username", user.getUsername())

                .issuedAt(issuedAt)
                .expiration(expirationDate)

                .signWith(getSigningKey())

                .compact();
    }

    /**
     * Extract username from JWT.
     */
    public String extractUsername(String token) {

        return getClaims(token)
                .getSubject();
    }

    /**
     * Extract user ID from JWT.
     */
    public Long extractUserId(String token) {

        Object userId = getClaims(token)
                .get("userId");

        if (userId == null) {
            return null;
        }

        return ((Number) userId).longValue();
    }

    /**
     * Check whether token is valid.
     */
    public boolean isTokenValid(String token) {

        try {
            getClaims(token);

            return true;

        } catch (JwtException |
                 IllegalArgumentException e) {
            System.err.println("Token validation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parse and validate JWT.
     */
    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}