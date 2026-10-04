package org.example.incentivebackend.module.approval.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.incentivebackend.common.audit.enums.AuditAction;
import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.audit.util.AuditHelper;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO;
import org.example.incentivebackend.module.approval.entity.*;
import org.example.incentivebackend.module.approval.enums.ApprovalActionType;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.repository.*;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalWorkflowLevelRepository workflowLevelRepository;
    private final ApprovalWorkflowApproverRepository workflowApproverRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalInstanceLevelRepository instanceLevelRepository;
    private final ApprovalInstanceApproverRepository instanceApproverRepository;
    private final ApprovalActionRepository actionRepository;
    private final DesignationPermissionService designationPermissionService;
    private final AuditLogService auditLogService;
    private final AuditHelper auditHelper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApprovalDetailsDTO submit(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String remarks) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user must be authenticated");
        }

        // Validate active workflow
        ApprovalWorkflowEntity workflow = workflowRepository.findByEntityTypeAndIsActiveTrue(entityType)
                .orElseThrow(() -> new IllegalStateException("No active approval workflow configured for " + entityType));

        List<ApprovalWorkflowLevelEntity> configuredLevels = workflowLevelRepository
                .findByWorkflow_IdAndIsActiveTrueOrderBySequenceOrderAscLevelNumberAsc(workflow.getId());

        if (configuredLevels == null || configuredLevels.isEmpty()) {
            throw new IllegalStateException("Workflow configuration error: No active approval levels defined");
        }

        // Validate workflow integrity
        validateWorkflowIntegrity(configuredLevels);

        // Check if there is already an active instance
        Optional<ApprovalInstanceEntity> activeOpt = instanceRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, ApprovalStatus.IN_PROGRESS);
        if (activeOpt.isPresent()) {
            throw new IllegalStateException("Approval instance already in progress for " + entityType + " #" + entityId);
        }

        // Calculate attempt number
        int attemptNumber = 1;
        Optional<ApprovalInstanceEntity> latestOpt = instanceRepository.findTopByEntityTypeAndEntityIdOrderByAttemptNumberDesc(
                entityType, entityId);
        if (latestOpt.isPresent()) {
            attemptNumber = latestOpt.get().getAttemptNumber() + 1;
        }

        // Check submit permission
        String pageCode = getPageCodeForEntity(entityType);
        if (!designationPermissionService.hasPermission(currentUser.getUserId(), pageCode, "SUBMIT")) {
            throw new SecurityException("User " + currentUser.getUsername() + " does not have SUBMIT permission for " + entityType);
        }

        // Create runtime ApprovalInstance
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setWorkflow(workflow);
        instance.setEntityType(entityType);
        instance.setEntityId(entityId);
        instance.setCurrentLevelNumber(1);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(currentUser);
        instance.setSubmittedAt(LocalDateTime.now());
        instance.setAttemptNumber(attemptNumber);
        instance = instanceRepository.save(instance);

        // Snapshot configured levels and approvers into runtime instance levels
        for (int i = 0; i < configuredLevels.size(); i++) {
            ApprovalWorkflowLevelEntity cfgLevel = configuredLevels.get(i);

            ApprovalInstanceLevelEntity instLevel = new ApprovalInstanceLevelEntity();
            instLevel.setApprovalInstance(instance);
            instLevel.setLevelNumber(cfgLevel.getLevelNumber());
            instLevel.setLevelName(cfgLevel.getLevelName());
            if (i == 0) {
                instLevel.setStatus(ApprovalStatus.ACTIVE);
                instLevel.setActivatedAt(LocalDateTime.now());
            } else {
                instLevel.setStatus(ApprovalStatus.PENDING);
            }
            instLevel = instanceLevelRepository.save(instLevel);

            // Snapshot approvers
            List<ApprovalWorkflowApproverEntity> cfgApprovers = workflowApproverRepository
                    .findByWorkflowLevel_IdAndIsActiveTrue(cfgLevel.getId());
            for (ApprovalWorkflowApproverEntity cfgAppr : cfgApprovers) {
                ApprovalInstanceApproverEntity instAppr = new ApprovalInstanceApproverEntity();
                instAppr.setInstanceLevel(instLevel);
                instAppr.setUser(cfgAppr.getUser());
                instanceApproverRepository.save(instAppr);
            }
        }

        // Record SUBMITTED action
        ApprovalActionEntity action = new ApprovalActionEntity();
        action.setApprovalInstance(instance);
        action.setLevelNumber(1);
        action.setLevelName(configuredLevels.get(0).getLevelName());
        action.setAction(attemptNumber > 1 ? ApprovalActionType.RESUBMITTED : ApprovalActionType.SUBMITTED);
        action.setActionBy(currentUser);
        action.setActionAt(LocalDateTime.now());
        action.setRemarks(remarks != null && !remarks.isBlank() ? remarks : "Submitted for approval");
        action.setAttemptNumber(attemptNumber);
        actionRepository.save(action);

        // Audit log
        auditLogService.createAuditLog(
                "APPROVAL", entityType.name(), "tr_approval_instance", instance.getId(),
                attemptNumber > 1 ? AuditAction.SUBMIT : AuditAction.SUBMIT,
                null, auditHelper.toJson(instance),
                "Workflow " + workflow.getWorkflowName() + " submitted (Attempt " + attemptNumber + ")",
                currentUser.getUserId(), String.valueOf(entityId), "APPROVAL", entityType.name(), entityId, "SUCCESS"
        );

        return buildDetailsDTO(instance, currentUser);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApprovalDetailsDTO approve(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String remarks) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user must be authenticated");
        }

        ApprovalInstanceEntity instance = instanceRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, ApprovalStatus.IN_PROGRESS)
                .orElseThrow(() -> new IllegalStateException("No active approval instance found for " + entityType + " #" + entityId));

        int currentLevelNum = instance.getCurrentLevelNumber();
        ApprovalInstanceLevelEntity currentLevel = instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(
                instance.getId(), currentLevelNum)
                .orElseThrow(() -> new IllegalStateException("Level " + currentLevelNum + " not found in instance"));

        // RULE 5 & 17: Check if level has already been completed
        if (currentLevel.getStatus() == ApprovalStatus.COMPLETED) {
            throw new IllegalStateException("Approval level has already been completed.");
        }

        if (currentLevel.getStatus() != ApprovalStatus.ACTIVE) {
            throw new IllegalStateException("Current level " + currentLevelNum + " is not ACTIVE (Status: " + currentLevel.getStatus() + ")");
        }

        // RULE 22: Maker-checker check
        if (Boolean.TRUE.equals(instance.getWorkflow().getRequireMakerChecker())) {
            if (currentUser.getUserId().equals(instance.getSubmittedBy().getUserId())) {
                throw new SecurityException("Maker-checker violation: Submitter cannot approve their own submission.");
            }
        }

        // RULE 3 & 23: Check APPROVE permission
        String pageCode = getPageCodeForEntity(entityType);
        if (!designationPermissionService.hasPermission(currentUser.getUserId(), pageCode, "APPROVE")) {
            throw new SecurityException("User " + currentUser.getUsername() + " does not have APPROVE permission for " + entityType);
        }

        // RULE 2 & 24: Check user assignment to this level snapshot
        boolean isAssigned = instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(
                currentLevel.getId(), currentUser.getUserId());
        if (!isAssigned) {
            throw new SecurityException("User " + currentUser.getUsername() + " is not an assigned approver for Level " +
                    currentLevel.getLevelNumber() + " (" + currentLevel.getLevelName() + ")");
        }

        try {
            // RULE 4 & 5: Complete current level
            currentLevel.setStatus(ApprovalStatus.COMPLETED);
            currentLevel.setCompletedAt(LocalDateTime.now());
            currentLevel.setCompletedBy(currentUser);
            currentLevel.setRemarks(remarks);
            instanceLevelRepository.saveAndFlush(currentLevel);

            // RULE 10 & 14: Record immutable action
            ApprovalActionEntity action = new ApprovalActionEntity();
            action.setApprovalInstance(instance);
            action.setLevelNumber(currentLevel.getLevelNumber());
            action.setLevelName(currentLevel.getLevelName());
            action.setAction(ApprovalActionType.APPROVED);
            action.setActionBy(currentUser);
            action.setActionAt(LocalDateTime.now());
            action.setRemarks(remarks != null && !remarks.isBlank() ? remarks : "Level approved");
            action.setAttemptNumber(instance.getAttemptNumber());
            actionRepository.save(action);

            // RULE 7: Sequential level progression
            Optional<ApprovalInstanceLevelEntity> nextLevelOpt = instanceLevelRepository
                    .findByApprovalInstance_IdAndLevelNumber(instance.getId(), currentLevelNum + 1);

            if (nextLevelOpt.isPresent()) {
                // Activate next level
                ApprovalInstanceLevelEntity nextLevel = nextLevelOpt.get();
                nextLevel.setStatus(ApprovalStatus.ACTIVE);
                nextLevel.setActivatedAt(LocalDateTime.now());
                instanceLevelRepository.save(nextLevel);

                instance.setCurrentLevelNumber(nextLevel.getLevelNumber());
                instanceRepository.save(instance);

                log.info("Level {} approved by {}. Level {} is now ACTIVE.", currentLevelNum, currentUser.getUsername(), nextLevel.getLevelNumber());
            } else {
                // All levels completed -> Workflow is APPROVED!
                instance.setStatus(ApprovalStatus.APPROVED);
                instance.setCompletedAt(LocalDateTime.now());
                instanceRepository.save(instance);

                log.info("Final Level {} approved by {}. Entire workflow is now APPROVED.", currentLevelNum, currentUser.getUsername());
            }

            // Audit event
            auditLogService.createAuditLog(
                    "APPROVAL", entityType.name(), "tr_approval_instance", instance.getId(),
                    AuditAction.APPROVE,
                    null, auditHelper.toJson(instance),
                    "Level " + currentLevel.getLevelNumber() + " (" + currentLevel.getLevelName() + ") approved by " + currentUser.getFullName(),
                    currentUser.getUserId(), String.valueOf(entityId), "APPROVAL", entityType.name(), entityId, "SUCCESS"
            );

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent approval detected on instance {} level {}", instance.getId(), currentLevelNum);
            throw new IllegalStateException("Approval level has already been completed by another approver.");
        }

        return buildDetailsDTO(instance, currentUser);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApprovalDetailsDTO reject(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String reason) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user must be authenticated");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is mandatory");
        }

        ApprovalInstanceEntity instance = instanceRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, ApprovalStatus.IN_PROGRESS)
                .orElseThrow(() -> new IllegalStateException("No active approval instance found for " + entityType + " #" + entityId));

        int currentLevelNum = instance.getCurrentLevelNumber();
        ApprovalInstanceLevelEntity currentLevel = instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(
                instance.getId(), currentLevelNum)
                .orElseThrow(() -> new IllegalStateException("Level " + currentLevelNum + " not found in instance"));

        if (currentLevel.getStatus() != ApprovalStatus.ACTIVE) {
            throw new IllegalStateException("Current level is not ACTIVE");
        }

        // Check REJECT permission
        String pageCode = getPageCodeForEntity(entityType);
        if (!designationPermissionService.hasPermission(currentUser.getUserId(), pageCode, "REJECT")) {
            throw new SecurityException("User " + currentUser.getUsername() + " does not have REJECT permission for " + entityType);
        }

        // Check user assignment to this level
        boolean isAssigned = instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(
                currentLevel.getId(), currentUser.getUserId());
        if (!isAssigned) {
            throw new SecurityException("User " + currentUser.getUsername() + " is not an assigned approver for current Level " +
                    currentLevel.getLevelNumber() + " (" + currentLevel.getLevelName() + ")");
        }

        // Mark current level REJECTED
        currentLevel.setStatus(ApprovalStatus.REJECTED);
        currentLevel.setCompletedAt(LocalDateTime.now());
        currentLevel.setCompletedBy(currentUser);
        currentLevel.setRemarks(reason);
        instanceLevelRepository.save(currentLevel);

        // RULE 8 & 20: Mark instance REJECTED, future levels remain PENDING and will never run
        instance.setStatus(ApprovalStatus.REJECTED);
        instance.setCompletedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        // Record REJECTED action
        ApprovalActionEntity action = new ApprovalActionEntity();
        action.setApprovalInstance(instance);
        action.setLevelNumber(currentLevel.getLevelNumber());
        action.setLevelName(currentLevel.getLevelName());
        action.setAction(ApprovalActionType.REJECTED);
        action.setActionBy(currentUser);
        action.setActionAt(LocalDateTime.now());
        action.setRemarks(reason);
        action.setAttemptNumber(instance.getAttemptNumber());
        actionRepository.save(action);

        // Audit log
        auditLogService.createAuditLog(
                "APPROVAL", entityType.name(), "tr_approval_instance", instance.getId(),
                AuditAction.REJECT,
                null, auditHelper.toJson(instance),
                "Workflow rejected at Level " + currentLevel.getLevelNumber() + " by " + currentUser.getFullName() + ": " + reason,
                currentUser.getUserId(), String.valueOf(entityId), "APPROVAL", entityType.name(), entityId, "SUCCESS"
        );

        return buildDetailsDTO(instance, currentUser);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ApprovalDetailsDTO cancel(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String reason) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user must be authenticated");
        }

        Optional<ApprovalInstanceEntity> opt = instanceRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, ApprovalStatus.IN_PROGRESS);
        if (opt.isEmpty()) {
            return getApprovalDetails(entityType, entityId, currentUser);
        }

        ApprovalInstanceEntity instance = opt.get();
        instance.setStatus(ApprovalStatus.CANCELLED);
        instance.setCompletedAt(LocalDateTime.now());
        instanceRepository.save(instance);

        ApprovalActionEntity action = new ApprovalActionEntity();
        action.setApprovalInstance(instance);
        action.setLevelNumber(instance.getCurrentLevelNumber());
        action.setAction(ApprovalActionType.CANCELLED);
        action.setActionBy(currentUser);
        action.setActionAt(LocalDateTime.now());
        action.setRemarks(reason != null ? reason : "Approval workflow cancelled");
        action.setAttemptNumber(instance.getAttemptNumber());
        actionRepository.save(action);

        auditLogService.createAuditLog(
                "APPROVAL", entityType.name(), "tr_approval_instance", instance.getId(),
                AuditAction.CANCEL,
                null, auditHelper.toJson(instance),
                "Workflow cancelled: " + reason,
                currentUser.getUserId(), String.valueOf(entityId), "APPROVAL", entityType.name(), entityId, "SUCCESS"
        );

        return buildDetailsDTO(instance, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalDetailsDTO getApprovalDetails(WorkflowEntityType entityType, Long entityId, UserEntity currentUser) {
        Optional<ApprovalInstanceEntity> latestOpt = instanceRepository.findTopByEntityTypeAndEntityIdOrderByAttemptNumberDesc(
                entityType, entityId);

        if (latestOpt.isEmpty()) {
            return null;
        }

        return buildDetailsDTO(latestOpt.get(), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveInstance(WorkflowEntityType entityType, Long entityId) {
        return instanceRepository.findByEntityTypeAndEntityIdAndStatus(
                entityType, entityId, ApprovalStatus.IN_PROGRESS).isPresent();
    }

    private void validateWorkflowIntegrity(List<ApprovalWorkflowLevelEntity> levels) {
        if (levels.isEmpty()) {
            throw new IllegalStateException("Approval workflow must have at least one level");
        }

        Set<Integer> levelNumbers = new HashSet<>();
        for (int i = 0; i < levels.size(); i++) {
            ApprovalWorkflowLevelEntity lvl = levels.get(i);
            int expectedNum = i + 1;
            if (lvl.getLevelNumber() == null || lvl.getLevelNumber() != expectedNum) {
                throw new IllegalStateException("Approval levels must be sequential starting at 1. Found level " + lvl.getLevelNumber() + " at index " + i);
            }
            if (!levelNumbers.add(lvl.getLevelNumber())) {
                throw new IllegalStateException("Duplicate level number found: " + lvl.getLevelNumber());
            }

            List<ApprovalWorkflowApproverEntity> approvers = workflowApproverRepository
                    .findByWorkflowLevel_IdAndIsActiveTrue(lvl.getId());
            if (approvers == null || approvers.isEmpty()) {
                throw new IllegalStateException("Workflow configuration error: Level " + lvl.getLevelNumber() + " (" + lvl.getLevelName() + ") has no assigned users");
            }

            Set<Long> userIds = new HashSet<>();
            for (ApprovalWorkflowApproverEntity app : approvers) {
                if (!userIds.add(app.getUser().getUserId())) {
                    throw new IllegalStateException("Workflow configuration error: Duplicate user " + app.getUser().getUsername() + " in Level " + lvl.getLevelNumber());
                }
            }
        }
    }

    private ApprovalDetailsDTO buildDetailsDTO(ApprovalInstanceEntity instance, UserEntity currentUser) {
        List<ApprovalInstanceLevelEntity> levels = instanceLevelRepository
                .findByApprovalInstance_IdOrderByLevelNumberAsc(instance.getId());

        List<ApprovalActionEntity> actions = actionRepository
                .findByApprovalInstance_IdOrderByActionAtAscIdAsc(instance.getId());

        ApprovalInstanceLevelEntity currentLevel = levels.stream()
                .filter(l -> l.getLevelNumber().equals(instance.getCurrentLevelNumber()))
                .findFirst()
                .orElse(null);

        boolean canApprove = false;
        boolean canReject = false;
        String disabledReason = null;

        if (currentUser != null && instance.getStatus() == ApprovalStatus.IN_PROGRESS && currentLevel != null) {
            String pageCode = getPageCodeForEntity(instance.getEntityType());
            boolean hasApprovePerm = designationPermissionService.hasPermission(currentUser.getUserId(), pageCode, "APPROVE");
            boolean hasRejectPerm = designationPermissionService.hasPermission(currentUser.getUserId(), pageCode, "REJECT");
            boolean isAssignedToLevel = instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(
                    currentLevel.getId(), currentUser.getUserId());
            boolean isSubmitter = currentUser.getUserId().equals(instance.getSubmittedBy().getUserId());
            boolean makerCheckerRestricted = Boolean.TRUE.equals(instance.getWorkflow().getRequireMakerChecker()) && isSubmitter;

            if (currentLevel.getStatus() != ApprovalStatus.ACTIVE) {
                disabledReason = "Current level is not active.";
            } else if (makerCheckerRestricted) {
                disabledReason = "Maker-checker policy: Submitter cannot approve their own submission.";
            } else if (!isAssignedToLevel) {
                disabledReason = "You are not an assigned approver for " + currentLevel.getLevelName() + " (Level " + currentLevel.getLevelNumber() + ").";
            } else if (!hasApprovePerm) {
                disabledReason = "You do not have approval permissions.";
            } else {
                canApprove = true;
            }

            if (!makerCheckerRestricted && isAssignedToLevel && hasRejectPerm && currentLevel.getStatus() == ApprovalStatus.ACTIVE) {
                canReject = true;
            }
        } else if (instance.getStatus() != ApprovalStatus.IN_PROGRESS) {
            disabledReason = "Workflow is already " + instance.getStatus();
        }

        List<ApprovalDetailsDTO.LevelProgressDTO> levelDTOs = levels.stream().map(lvl -> {
            List<ApprovalInstanceApproverEntity> approverEntities = instanceApproverRepository.findByInstanceLevel_Id(lvl.getId());
            List<ApprovalDetailsDTO.ApproverUserDTO> approverDTOs = approverEntities.stream().map(app ->
                    ApprovalDetailsDTO.ApproverUserDTO.builder()
                            .userId(app.getUser().getUserId())
                            .username(app.getUser().getUsername())
                            .fullName(app.getUser().getFullName())
                            .designation(app.getUser().getDesignation() != null ? app.getUser().getDesignation().getDesignationName() : null)
                            .hasApproved(lvl.getCompletedBy() != null && lvl.getCompletedBy().getUserId().equals(app.getUser().getUserId()))
                            .build()
            ).collect(Collectors.toList());

            return ApprovalDetailsDTO.LevelProgressDTO.builder()
                    .levelNumber(lvl.getLevelNumber())
                    .levelName(lvl.getLevelName())
                    .status(lvl.getStatus())
                    .activatedAt(lvl.getActivatedAt())
                    .completedAt(lvl.getCompletedAt())
                    .completedById(lvl.getCompletedBy() != null ? lvl.getCompletedBy().getUserId() : null)
                    .completedByName(lvl.getCompletedBy() != null ? lvl.getCompletedBy().getFullName() : null)
                    .remarks(lvl.getRemarks())
                    .assignedApprovers(approverDTOs)
                    .build();
        }).collect(Collectors.toList());

        List<ApprovalDetailsDTO.ApprovalActionDTO> actionDTOs = actions.stream().map(act ->
                ApprovalDetailsDTO.ApprovalActionDTO.builder()
                        .id(act.getId())
                        .levelNumber(act.getLevelNumber())
                        .levelName(act.getLevelName())
                        .action(act.getAction())
                        .actionById(act.getActionBy().getUserId())
                        .actionByName(act.getActionBy().getFullName())
                        .actionAt(act.getActionAt())
                        .remarks(act.getRemarks())
                        .attemptNumber(act.getAttemptNumber())
                        .build()
        ).collect(Collectors.toList());

        return ApprovalDetailsDTO.builder()
                .instanceId(instance.getId())
                .workflowName(instance.getWorkflow().getWorkflowName())
                .entityType(instance.getEntityType())
                .entityId(instance.getEntityId())
                .status(instance.getStatus())
                .currentLevelNumber(instance.getCurrentLevelNumber())
                .totalLevels(levels.size())
                .currentLevelName(currentLevel != null ? currentLevel.getLevelName() : null)
                .canApprove(canApprove)
                .canReject(canReject)
                .approvalDisabledReason(disabledReason)
                .submittedById(instance.getSubmittedBy().getUserId())
                .submittedByName(instance.getSubmittedBy().getFullName())
                .submittedAt(instance.getSubmittedAt())
                .completedAt(instance.getCompletedAt())
                .attemptNumber(instance.getAttemptNumber())
                .levels(levelDTOs)
                .history(actionDTOs)
                .build();
    }

    private String getPageCodeForEntity(WorkflowEntityType entityType) {
        return switch (entityType) {
            case PARTY_PAYMENT -> "PARTY_PAYMENT";
            case BILL -> "BILL_ENTRY";
            case ACCRUED_PAYABLE -> "PARTY_ENTRY";
            case CUSTOMER_PAYMENT -> "CUSTOMER_PAYMENT";
            default -> entityType.name();
        };
    }
}
