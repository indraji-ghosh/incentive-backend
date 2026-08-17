package org.example.incentivebackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // A valid 256-bit test secret (32 bytes) encoded in Base64
    private final String TEST_SECRET = Base64.getEncoder().encodeToString(
            "TEST_SECRET_KEY_THAT_IS_AT_LEAST_32_BYTES_LONG".getBytes()
    );

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnValidJwt() {
        UserEntity user = new UserEntity();
        user.setUserId(10L);
        user.setUsername("testuser");

        String token = jwtService.generateToken(user);
        
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        UserEntity user = new UserEntity();
        user.setUserId(10L);
        user.setUsername("testuser");

        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        
        assertEquals("testuser", username);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        UserEntity user = new UserEntity();
        user.setUserId(99L);
        user.setUsername("testuser");

        String token = jwtService.generateToken(user);
        Long userId = jwtService.extractUserId(token);
        
        assertEquals(99L, userId);
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        // Set a 1 millisecond expiration just for this test
        ReflectionTestUtils.setField(jwtService, "expiration", 1L);
        
        UserEntity user = new UserEntity();
        user.setUserId(10L);
        user.setUsername("testuser");

        String token = jwtService.generateToken(user);
        
        // Wait 10 ms to ensure it's expired
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithTamperedToken_ShouldReturnFalse() {
        UserEntity user = new UserEntity();
        user.setUserId(10L);
        user.setUsername("testuser");

        String token = jwtService.generateToken(user);
        String tamperedToken = token + "tamper";
        
        assertFalse(jwtService.isTokenValid(tamperedToken));
    }

    @Test
    void isTokenValid_WithInvalidSignature_ShouldReturnFalse() {
        // Generate a token with a completely different secret
        SecretKey differentKey = Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString("ANOTHER_TEST_SECRET_KEY_THAT_IS_AT_LEAST_32_BYTES".getBytes()).getBytes());
        String tokenWithDifferentSignature = Jwts.builder()
                .subject("testuser")
                .signWith(differentKey)
                .compact();

        assertFalse(jwtService.isTokenValid(tokenWithDifferentSignature));
    }
}
