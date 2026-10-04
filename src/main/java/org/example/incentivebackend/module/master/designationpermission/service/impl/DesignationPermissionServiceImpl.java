package org.example.incentivebackend.module.master.designationpermission.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.auth.dto.EffectivePermissionResponseDTO;
import org.example.incentivebackend.module.master.designation.dto.DesignationResponseDTO;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.designation.repository.DesignationRepository;
import org.example.incentivebackend.module.master.designationpermission.dto.DesignationPermissionRequestDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.DesignationPermissionResponseDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.PermissionMatrixRequestDTO;
import org.example.incentivebackend.module.master.designationpermission.entity.DesignationPagePermissionEntity;
import org.example.incentivebackend.module.master.designationpermission.repository.DesignationPagePermissionRepository;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.example.incentivebackend.module.master.page.entity.PageEntity;
import org.example.incentivebackend.module.master.page.repository.PageRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignationPermissionServiceImpl implements DesignationPermissionService {

    private final DesignationPagePermissionRepository permissionRepository;
    private final DesignationRepository designationRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;

    @Override
    public List<DesignationPermissionResponseDTO> getPermissionsByDesignationId(Long designationId) {
        DesignationEntity designation = designationRepository.findById(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + designationId));

        List<PageEntity> allPages = pageRepository.findAllByOrderByDisplayOrderAsc();
        List<DesignationPagePermissionEntity> existingPermissions = permissionRepository.findByDesignationIdWithPage(designationId);

        List<DesignationPermissionResponseDTO> result = new ArrayList<>();
        for (PageEntity page : allPages) {
            Optional<DesignationPagePermissionEntity> optPerm = existingPermissions.stream()
                    .filter(p -> p.getPage().getId().equals(page.getId()))
                    .findFirst();
            
            if (optPerm.isPresent()) {
                DesignationPagePermissionEntity perm = optPerm.get();
                result.add(DesignationPermissionResponseDTO.builder()
                        .pageId(page.getId())
                        .pageCode(page.getPageCode())
                        .pageName(page.getPageName())
                        .canView(perm.getCanView())
                        .canCreate(perm.getCanCreate())
                        .canEdit(perm.getCanEdit())
                        .canDelete(perm.getCanDelete())
                        .canExport(perm.getCanExport())
                        .canSubmit(perm.getCanSubmit())
                        .canApprove(perm.getCanApprove())
                        .canReject(perm.getCanReject())
                        .build());
            } else {
                result.add(DesignationPermissionResponseDTO.builder()
                        .pageId(page.getId())
                        .pageCode(page.getPageCode())
                        .pageName(page.getPageName())
                        .canView(false)
                        .canCreate(false)
                        .canEdit(false)
                        .canDelete(false)
                        .canExport(false)
                        .canSubmit(false)
                        .canApprove(false)
                        .canReject(false)
                        .build());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void updatePermissionMatrix(Long designationId, PermissionMatrixRequestDTO request) {
        DesignationEntity designation = designationRepository.findById(designationId)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + designationId));

        for (DesignationPermissionRequestDTO reqPerm : request.getPermissions()) {
            PageEntity page = pageRepository.findById(reqPerm.getPageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + reqPerm.getPageId()));
            
            // Dependency resolution
            boolean canView = Boolean.TRUE.equals(reqPerm.getCanView());
            boolean canCreate = Boolean.TRUE.equals(reqPerm.getCanCreate());
            boolean canEdit = Boolean.TRUE.equals(reqPerm.getCanEdit());
            boolean canDelete = Boolean.TRUE.equals(reqPerm.getCanDelete());
            boolean canExport = Boolean.TRUE.equals(reqPerm.getCanExport());
            boolean canSubmit = Boolean.TRUE.equals(reqPerm.getCanSubmit());
            boolean canApprove = Boolean.TRUE.equals(reqPerm.getCanApprove());
            boolean canReject = Boolean.TRUE.equals(reqPerm.getCanReject());

            if (canCreate || canEdit || canDelete || canExport || canSubmit || canApprove || canReject) {
                canView = true; // Auto-resolve dependency
            }

            Optional<DesignationPagePermissionEntity> optExisting = permissionRepository.findByDesignationIdAndPageId(designationId, page.getId());
            
            DesignationPagePermissionEntity entity;
            if (optExisting.isPresent()) {
                entity = optExisting.get();
                entity.setCanView(canView);
                entity.setCanCreate(canCreate);
                entity.setCanEdit(canEdit);
                entity.setCanDelete(canDelete);
                entity.setCanExport(canExport);
                entity.setCanSubmit(canSubmit);
                entity.setCanApprove(canApprove);
                entity.setCanReject(canReject);
            } else {
                entity = DesignationPagePermissionEntity.builder()
                        .designation(designation)
                        .page(page)
                        .canView(canView)
                        .canCreate(canCreate)
                        .canEdit(canEdit)
                        .canDelete(canDelete)
                        .canExport(canExport)
                        .canSubmit(canSubmit)
                        .canApprove(canApprove)
                        .canReject(canReject)
                        .build();
            }
            permissionRepository.save(entity);
        }
    }

    @Override
    public EffectivePermissionResponseDTO getEffectivePermissionsForUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DesignationEntity designation = user.getDesignation();
        if (designation == null) {
            return EffectivePermissionResponseDTO.builder()
                    .designation(null)
                    .permissions(new ArrayList<>())
                    .build();
        }

        DesignationResponseDTO designationDto = DesignationResponseDTO.builder()
                .id(designation.getId())
                .designationCode(designation.getDesignationCode())
                .designationName(designation.getDesignationName())
                .level(designation.getLevel())
                .description(designation.getDescription())
                .isActive(designation.getIsActive())
                .build();

        List<DesignationPermissionResponseDTO> perms = getPermissionsByDesignationId(designation.getId());
        
        return EffectivePermissionResponseDTO.builder()
                .designation(designationDto)
                .permissions(perms)
                .build();
    }

    @Override
    public boolean hasPermission(Long userId, String pageCode, String permissionType) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        
        // Super admin bypass - assume username "admin" or similar role check.
        // If your system has a RoleEntity, check it here. For now, let's say username "admin" is super admin.
        if ("admin".equals(user.getUsername())) return true;
        
        DesignationEntity designation = user.getDesignation();
        if (designation == null) return false;
        
        Optional<DesignationPagePermissionEntity> perm = permissionRepository.findByDesignationIdAndPageCode(designation.getId(), pageCode);
        if (perm.isEmpty()) return false;
        
        DesignationPagePermissionEntity entity = perm.get();
        return switch (permissionType.toUpperCase()) {
            case "VIEW" -> Boolean.TRUE.equals(entity.getCanView());
            case "CREATE" -> Boolean.TRUE.equals(entity.getCanCreate());
            case "EDIT" -> Boolean.TRUE.equals(entity.getCanEdit());
            case "DELETE" -> Boolean.TRUE.equals(entity.getCanDelete());
            case "EXPORT" -> Boolean.TRUE.equals(entity.getCanExport());
            case "SUBMIT" -> Boolean.TRUE.equals(entity.getCanSubmit());
            case "APPROVE" -> Boolean.TRUE.equals(entity.getCanApprove());
            case "REJECT" -> Boolean.TRUE.equals(entity.getCanReject());
            default -> false;
        };
    }
}
