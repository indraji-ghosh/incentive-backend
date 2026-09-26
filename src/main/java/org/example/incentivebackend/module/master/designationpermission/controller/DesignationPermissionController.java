package org.example.incentivebackend.module.master.designationpermission.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.constant.ApiConstants;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.designationpermission.dto.DesignationPermissionResponseDTO;
import org.example.incentivebackend.module.master.designationpermission.dto.PermissionMatrixRequestDTO;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/permissions")
@RequiredArgsConstructor
public class DesignationPermissionController {

    private final DesignationPermissionService permissionService;

    @GetMapping("/designation/{designationId}")
    @PreAuthorize("@perm.has(authentication, 'PERMISSION_MATRIX', 'VIEW')")
    public ResponseEntity<ApiResponse<List<DesignationPermissionResponseDTO>>> getPermissionsByDesignationId(
            @PathVariable Long designationId) {
        List<DesignationPermissionResponseDTO> data = permissionService.getPermissionsByDesignationId(designationId);
        return ResponseBuilder.list("Permissions", data);
    }

    @PutMapping("/designation/{designationId}")
    @PreAuthorize("@perm.has(authentication, 'PERMISSION_MATRIX', 'UPDATE')")
    public ResponseEntity<ApiResponse<Void>> updatePermissionMatrix(
            @PathVariable Long designationId,
            @RequestBody PermissionMatrixRequestDTO request) {
        permissionService.updatePermissionMatrix(designationId, request);
        return ResponseBuilder.updated("Permissions", null);
    }
}
