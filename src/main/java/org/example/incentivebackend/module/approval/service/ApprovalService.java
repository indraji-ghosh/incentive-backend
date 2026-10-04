package org.example.incentivebackend.module.approval.service;

import org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.master.user.UserEntity;

public interface ApprovalService {

    ApprovalDetailsDTO submit(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String remarks);

    ApprovalDetailsDTO approve(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String remarks);

    ApprovalDetailsDTO reject(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String reason);

    ApprovalDetailsDTO cancel(WorkflowEntityType entityType, Long entityId, UserEntity currentUser, String reason);

    ApprovalDetailsDTO getApprovalDetails(WorkflowEntityType entityType, Long entityId, UserEntity currentUser);

    boolean hasActiveInstance(WorkflowEntityType entityType, Long entityId);
}
