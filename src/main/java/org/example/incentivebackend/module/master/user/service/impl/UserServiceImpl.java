package org.example.incentivebackend.module.master.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.designation.repository.DesignationRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.master.user.UserStatus;
import org.example.incentivebackend.module.master.user.dto.UserRequestDTO;
import org.example.incentivebackend.module.master.user.dto.UserResponseDTO;
import org.example.incentivebackend.module.master.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DesignationRepository designationRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.example.incentivebackend.common.audit.service.AuditLogService auditLogService;
    private final org.example.incentivebackend.common.audit.util.AuditHelper auditHelper;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        UserEntity entity = new UserEntity();
        entity.setUsername(request.getUsername());
        entity.setFullName(request.getEmployeeName());
        entity.setEmail(request.getEmail());
        entity.setMobileNumber(request.getMobileNumber());
        entity.setRole(request.getRole());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            entity.setPassword(passwordEncoder.encode("password"));
        }
        
        entity.setStatus(Boolean.TRUE.equals(request.getIsActive()) ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        
        if (request.getDesignation() != null && !request.getDesignation().isEmpty()) {
            DesignationEntity desig = designationRepository.findByDesignationName(request.getDesignation())
                .orElse(null);
            entity.setDesignation(desig);
        }
        
        UserEntity saved = userRepository.save(entity);
        UserResponseDTO response = mapToDTO(saved);
        
        auditLogService.createAuditLog(
            "MASTER", "User", "ms_users", saved.getUserId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.CREATE,
            null, auditHelper.toJson(response),
            "User created", 1L,
            saved.getUsername(), null, null, null, "SUCCESS"
        );

        return response;
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        String oldStateJson = auditHelper.toJson(mapToDTO(entity));
                
        if (!entity.getUsername().equalsIgnoreCase(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        
        entity.setUsername(request.getUsername());
        entity.setFullName(request.getEmployeeName());
        entity.setEmail(request.getEmail());
        entity.setMobileNumber(request.getMobileNumber());
        entity.setRole(request.getRole());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        entity.setStatus(Boolean.TRUE.equals(request.getIsActive()) ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        
        if (request.getDesignation() != null && !request.getDesignation().isEmpty()) {
            DesignationEntity desig = designationRepository.findByDesignationName(request.getDesignation())
                .orElse(null);
            entity.setDesignation(desig);
        } else {
            entity.setDesignation(null);
        }
        
        UserEntity saved = userRepository.save(entity);
        UserResponseDTO response = mapToDTO(saved);
        
        auditLogService.createAuditLog(
            "MASTER", "User", "ms_users", saved.getUserId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.UPDATE,
            oldStateJson, auditHelper.toJson(response),
            "User updated", 1L,
            saved.getUsername(), null, null, null, "SUCCESS"
        );

        return response;
    }

    @Override
    public void deleteUser(Long id) {
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        String oldStateJson = auditHelper.toJson(mapToDTO(entity));
        userRepository.deleteById(id);
        
        auditLogService.createAuditLog(
            "MASTER", "User", "ms_users", id,
            org.example.incentivebackend.common.audit.enums.AuditAction.DELETE,
            oldStateJson, null,
            "User deleted", 1L,
            entity.getUsername(), null, null, null, "SUCCESS"
        );
    }

    private UserResponseDTO mapToDTO(UserEntity entity) {
        return UserResponseDTO.builder()
                .id(entity.getUserId())
                .username(entity.getUsername())
                .employeeName(entity.getFullName())
                .email(entity.getEmail())
                .mobileNumber(entity.getMobileNumber())
                .role(entity.getRole())
                .designation(entity.getDesignation() != null ? entity.getDesignation().getDesignationName() : null)
                .isActive(entity.getStatus() == UserStatus.ACTIVE)
                .build();
    }
}
