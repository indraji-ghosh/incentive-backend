package org.example.incentivebackend.module.approval.service;

import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowRequestDTO;
import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowResponseDTO;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;

import java.util.List;

public interface ApprovalWorkflowAdminService {
    List<ApprovalWorkflowResponseDTO> getAllWorkflows();
    ApprovalWorkflowResponseDTO getWorkflowById(Long id);
    ApprovalWorkflowResponseDTO getActiveWorkflowByEntityType(WorkflowEntityType entityType);
    ApprovalWorkflowResponseDTO createWorkflow(ApprovalWorkflowRequestDTO request);
    ApprovalWorkflowResponseDTO updateWorkflow(Long id, ApprovalWorkflowRequestDTO request);
}
