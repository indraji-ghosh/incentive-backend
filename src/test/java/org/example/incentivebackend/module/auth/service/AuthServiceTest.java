package org.example.incentivebackend.module.auth.service;

import org.example.incentivebackend.config.JwtService;
import org.example.incentivebackend.module.auth.dto.LoginRequest;
import org.example.incentivebackend.module.auth.dto.LoginResponse;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.master.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        UserEntity user = new UserEntity();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setPassword("encoded_password");
        user.setFullName("Admin User");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "encoded_password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt.token.here");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("admin", response.getUsername());
        assertEquals("Admin User", response.getFullName());
    }

    @Test
    void login_WithMissingUser_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
        
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WithWrongPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("WrongPassword");

        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPassword("encoded_password");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encoded_password")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WithInactiveUser_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User is not active", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_WithLockedUser_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123");

        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setStatus(UserStatus.LOCKED);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("User is not active", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }
}
