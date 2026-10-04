package org.example.incentivebackend.module.approval.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowRequestDTO;
import org.example.incentivebackend.module.approval.dto.ApprovalWorkflowResponseDTO;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowApproverEntity;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowEntity;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowLevelEntity;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowApproverRepository;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowLevelRepository;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalWorkflowAdminServiceImpl implements ApprovalWorkflowAdminService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalWorkflowLevelRepository levelRepository;
    private final ApprovalWorkflowApproverRepository approverRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalWorkflowResponseDTO> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflowResponseDTO getWorkflowById(Long id) {
        ApprovalWorkflowEntity entity = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));
        return mapToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalWorkflowResponseDTO getActiveWorkflowByEntityType(WorkflowEntityType entityType) {
        ApprovalWorkflowEntity entity = workflowRepository.findByEntityTypeAndIsActiveTrue(entityType)
                .orElseThrow(() -> new ResourceNotFoundException("No active workflow for entity type: " + entityType));
        return mapToDTO(entity);
    }

    @Override
    @Transactional
    public ApprovalWorkflowResponseDTO createWorkflow(ApprovalWorkflowRequestDTO request) {
        validateRequest(request);

        if (workflowRepository.existsByEntityTypeAndWorkflowNameIgnoreCase(request.getEntityType(), request.getWorkflowName())) {
            throw new IllegalArgumentException("Workflow with name '" + request.getWorkflowName() + "' already exists for " + request.getEntityType());
        }

        // If newly created workflow is active, deactivate existing ones for the same entityType
        if (Boolean.TRUE.equals(request.getIsActive())) {
            workflowRepository.findByEntityTypeAndIsActiveTrue(request.getEntityType()).ifPresent(existing -> {
                existing.setIsActive(false);
                workflowRepository.save(existing);
            });
        }

        ApprovalWorkflowEntity workflow = new ApprovalWorkflowEntity();
        workflow.setWorkflowName(request.getWorkflowName());
        workflow.setEntityType(request.getEntityType());
        workflow.setDescription(request.getDescription());
        workflow.setIsActive(request.getIsActive());
        workflow.setRequireMakerChecker(request.getRequireMakerChecker());
        workflow = workflowRepository.save(workflow);

        saveLevelsAndApprovers(workflow, request.getLevels());

        return mapToDTO(workflow);
    }

    @Override
    @Transactional
    public ApprovalWorkflowResponseDTO updateWorkflow(Long id, ApprovalWorkflowRequestDTO request) {
        validateRequest(request);

        ApprovalWorkflowEntity workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval workflow not found with id: " + id));

        // If activating this workflow, deactivate any other active one for the same entityType
        if (Boolean.TRUE.equals(request.getIsActive()) && !Boolean.TRUE.equals(workflow.getIsActive())) {
            workflowRepository.findByEntityTypeAndIsActiveTrue(request.getEntityType()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    existing.setIsActive(false);
                    workflowRepository.save(existing);
                }
            });
        }

        workflow.setWorkflowName(request.getWorkflowName());
        workflow.setDescription(request.getDescription());
        workflow.setIsActive(request.getIsActive());
        workflow.setRequireMakerChecker(request.getRequireMakerChecker());
        workflow = workflowRepository.save(workflow);

        // Delete existing levels
        List<ApprovalWorkflowLevelEntity> existingLevels = levelRepository.findByWorkflow_IdAndIsActiveTrueOrderBySequenceOrderAscLevelNumberAsc(id);
        for (ApprovalWorkflowLevelEntity lvl : existingLevels) {
            approverRepository.deleteAll(approverRepository.findByWorkflowLevel_IdAndIsActiveTrue(lvl.getId()));
            levelRepository.delete(lvl);
        }

        saveLevelsAndApprovers(workflow, request.getLevels());

        return mapToDTO(workflow);
    }

    private void saveLevelsAndApprovers(ApprovalWorkflowEntity workflow, List<ApprovalWorkflowRequestDTO.WorkflowLevelRequestDTO> levelDTOs) {
        for (ApprovalWorkflowRequestDTO.WorkflowLevelRequestDTO lvlDto : levelDTOs) {
            ApprovalWorkflowLevelEntity level = new ApprovalWorkflowLevelEntity();
            level.setWorkflow(workflow);
            level.setLevelNumber(lvlDto.getLevelNumber());
            level.setLevelName(lvlDto.getLevelName());
            level.setSequenceOrder(lvlDto.getSequenceOrder());
            level.setIsActive(true);
            level = levelRepository.save(level);

            for (Long userId : lvlDto.getApproverUserIds()) {
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                ApprovalWorkflowApproverEntity approver = new ApprovalWorkflowApproverEntity();
                approver.setWorkflowLevel(level);
                approver.setUser(user);
                approver.setIsActive(true);
                approverRepository.save(approver);
            }
        }
    }

    private void validateRequest(ApprovalWorkflowRequestDTO request) {
        if (request.getLevels() == null || request.getLevels().isEmpty()) {
            throw new IllegalArgumentException("Workflow must have at least one level");
        }

        Set<Integer> levelNums = new HashSet<>();
        for (int i = 0; i < request.getLevels().size(); i++) {
            ApprovalWorkflowRequestDTO.WorkflowLevelRequestDTO lvl = request.getLevels().get(i);
            int expected = i + 1;
            if (lvl.getLevelNumber() == null || lvl.getLevelNumber() != expected) {
                throw new IllegalArgumentException("Levels must be sequential starting at 1. Expected " + expected + ", got " + lvl.getLevelNumber());
            }
            if (!levelNums.add(lvl.getLevelNumber())) {
                throw new IllegalArgumentException("Duplicate level number: " + lvl.getLevelNumber());
            }

            if (lvl.getApproverUserIds() == null || lvl.getApproverUserIds().isEmpty()) {
                throw new IllegalArgumentException("Level " + lvl.getLevelNumber() + " must have at least one approver user");
            }

            Set<Long> userIds = new HashSet<>();
            for (Long uId : lvl.getApproverUserIds()) {
                if (!userIds.add(uId)) {
                    throw new IllegalArgumentException("Duplicate user ID " + uId + " in Level " + lvl.getLevelNumber());
                }
            }
        }
    }

    private ApprovalWorkflowResponseDTO mapToDTO(ApprovalWorkflowEntity workflow) {
        List<ApprovalWorkflowLevelEntity> levels = levelRepository
                .findByWorkflow_IdAndIsActiveTrueOrderBySequenceOrderAscLevelNumberAsc(workflow.getId());

        List<ApprovalWorkflowResponseDTO.WorkflowLevelResponseDTO> levelDTOs = levels.stream().map(lvl -> {
            List<ApprovalWorkflowApproverEntity> approvers = approverRepository
                    .findByWorkflowLevel_IdAndIsActiveTrue(lvl.getId());

            List<ApprovalWorkflowResponseDTO.WorkflowApproverResponseDTO> approverDTOs = approvers.stream().map(app ->
                    ApprovalWorkflowResponseDTO.WorkflowApproverResponseDTO.builder()
                            .id(app.getId())
                            .userId(app.getUser().getUserId())
                            .username(app.getUser().getUsername())
                            .fullName(app.getUser().getFullName())
                            .designation(app.getUser().getDesignation() != null ? app.getUser().getDesignation().getDesignationName() : null)
                            .build()
            ).collect(Collectors.toList());

            return ApprovalWorkflowResponseDTO.WorkflowLevelResponseDTO.builder()
                    .id(lvl.getId())
                    .levelNumber(lvl.getLevelNumber())
                    .levelName(lvl.getLevelName())
                    .sequenceOrder(lvl.getSequenceOrder())
                    .isActive(lvl.getIsActive())
                    .approvers(approverDTOs)
                    .build();
        }).collect(Collectors.toList());

        return ApprovalWorkflowResponseDTO.builder()
                .id(workflow.getId())
                .workflowName(workflow.getWorkflowName())
                .entityType(workflow.getEntityType())
                .description(workflow.getDescription())
                .isActive(workflow.getIsActive())
                .requireMakerChecker(workflow.getRequireMakerChecker())
                .levels(levelDTOs)
                .build();
    }
}
