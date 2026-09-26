package org.example.incentivebackend.module.master.designation.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.constant.ApiConstants;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.designation.dto.DesignationRequestDTO;
import org.example.incentivebackend.module.master.designation.dto.DesignationResponseDTO;
import org.example.incentivebackend.module.master.designation.service.DesignationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/master/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'DESIGNATION_MASTER', 'VIEW') or @perm.has(authentication, 'PERMISSION_MATRIX', 'VIEW')")
    public ResponseEntity<ApiResponse<List<DesignationResponseDTO>>> getAllDesignations() {
        List<DesignationResponseDTO> data = designationService.getAllDesignations();
        return ResponseBuilder.list("Designations", data);
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'DESIGNATION_MASTER', 'ADD')")
    public ResponseEntity<ApiResponse<DesignationResponseDTO>> createDesignation(@RequestBody DesignationRequestDTO request) {
        DesignationResponseDTO data = designationService.createDesignation(request);
        return ResponseBuilder.created("Designation", data);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'DESIGNATION_MASTER', 'UPDATE')")
    public ResponseEntity<ApiResponse<DesignationResponseDTO>> updateDesignation(
            @PathVariable Long id, @RequestBody DesignationRequestDTO request) {
        DesignationResponseDTO data = designationService.updateDesignation(id, request);
        return ResponseBuilder.updated("Designation", data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'DESIGNATION_MASTER', 'DELETE')")
    public ResponseEntity<ApiResponse<Object>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseBuilder.deleted("Designation");
    }
}
