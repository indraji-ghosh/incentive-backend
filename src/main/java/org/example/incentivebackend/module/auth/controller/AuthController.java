package org.example.incentivebackend.module.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.auth.dto.LoginRequest;
import org.example.incentivebackend.module.auth.dto.LoginResponse;
import org.example.incentivebackend.module.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }
}
