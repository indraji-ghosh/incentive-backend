package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowEntity;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflowEntity, Long> {
    Optional<ApprovalWorkflowEntity> findByEntityTypeAndIsActiveTrue(WorkflowEntityType entityType);
    boolean existsByEntityTypeAndWorkflowNameIgnoreCase(WorkflowEntityType entityType, String workflowName);
}
