package org.example.incentivebackend.module.approval.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowRequestDTO;
import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowResponseDTO;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.service.ApprovalWorkflowAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval/workflows")
@RequiredArgsConstructor
public class ApprovalWorkflowAdminController {

    private final ApprovalWorkflowAdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApprovalWorkflowResponseDTO>>> getAll() {
        List<ApprovalWorkflowResponseDTO> response = adminService.getAllWorkflows();
        return ResponseBuilder.list("Approval Workflows", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalWorkflowResponseDTO>> getById(@PathVariable Long id) {
        ApprovalWorkflowResponseDTO response = adminService.getWorkflowById(id);
        return ResponseBuilder.fetched("Approval Workflow", response);
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<ApiResponse<ApprovalWorkflowResponseDTO>> getActiveByEntityType(@PathVariable WorkflowEntityType entityType) {
        ApprovalWorkflowResponseDTO response = adminService.getActiveWorkflowByEntityType(entityType);
        return ResponseBuilder.fetched("Active Approval Workflow", response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalWorkflowResponseDTO>> create(@Valid @RequestBody ApprovalWorkflowRequestDTO request) {
        ApprovalWorkflowResponseDTO response = adminService.createWorkflow(request);
        return ResponseBuilder.created("Approval Workflow", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalWorkflowResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalWorkflowRequestDTO request) {
        ApprovalWorkflowResponseDTO response = adminService.updateWorkflow(id, request);
        return ResponseBuilder.updated("Approval Workflow", response);
    }
}
