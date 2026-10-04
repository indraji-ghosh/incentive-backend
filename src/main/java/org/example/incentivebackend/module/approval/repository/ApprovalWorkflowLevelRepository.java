package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalWorkflowLevelRepository extends JpaRepository<ApprovalWorkflowLevelEntity, Long> {
    List<ApprovalWorkflowLevelEntity> findByWorkflow_IdAndIsActiveTrueOrderBySequenceOrderAscLevelNumberAsc(Long workflowId);
    Optional<ApprovalWorkflowLevelEntity> findByWorkflow_IdAndLevelNumber(Long workflowId, Integer levelNumber);
}
