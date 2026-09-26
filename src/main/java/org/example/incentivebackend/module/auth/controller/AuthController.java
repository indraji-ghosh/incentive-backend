package org.example.incentivebackend.module.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.auth.dto.LoginRequest;
import org.example.incentivebackend.module.auth.dto.LoginResponse;
import org.example.incentivebackend.module.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.auth.dto.EffectivePermissionResponseDTO;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DesignationPermissionService designationPermissionService;
    private final org.example.incentivebackend.module.master.user.UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @GetMapping("/me/permissions")
    public ResponseEntity<ApiResponse<EffectivePermissionResponseDTO>> getMyPermissions(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.<EffectivePermissionResponseDTO>builder()
                .success(false)
                .message("Unauthorized")
                .build());
        }
        org.example.incentivebackend.module.master.user.UserEntity user = 
            userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null || user.getUserId() == null) {
            return ResponseEntity.status(401).body(ApiResponse.<EffectivePermissionResponseDTO>builder()
                .success(false)
                .message("Unauthorized")
                .build());
        }
        EffectivePermissionResponseDTO perms = designationPermissionService.getEffectivePermissionsForUser(user.getUserId());
        return ResponseBuilder.fetched("Permissions", perms);
    }
}
