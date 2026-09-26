package org.example.incentivebackend.module.master.designationpermission.service;

import org.example.incentivebackend.module.auth.dto.EffectivePermissionResponseDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.DesignationPermissionResponseDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.PermissionMatrixRequestDTO;

import java.util.List;

public interface DesignationPermissionService {
    List<DesignationPermissionResponseDTO> getPermissionsByDesignationId(Long designationId);
    void updatePermissionMatrix(Long designationId, PermissionMatrixRequestDTO request);
    EffectivePermissionResponseDTO getEffectivePermissionsForUser(Long userId);
    boolean hasPermission(Long userId, String pageCode, String permissionType);
}
