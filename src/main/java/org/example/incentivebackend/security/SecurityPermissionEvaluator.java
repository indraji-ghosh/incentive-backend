package org.example.incentivebackend.security;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.springframework.security.core.userdetails.UserDetails;
import org.example.incentivebackend.module.master.user.UserRepository;

@Component("perm")
@RequiredArgsConstructor
public class SecurityPermissionEvaluator {

    private final DesignationPermissionService permissionService;
    private final UserRepository userRepository;

    public boolean has(Authentication authentication, String pageCode, String permissionType) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        String username = null;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String str) {
            username = str;
        }

        if (username == null) {
            return false;
        }

        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getUserId() == null) {
            return false;
        }

        return permissionService.hasPermission(user.getUserId(), pageCode, permissionType);
    }
}
