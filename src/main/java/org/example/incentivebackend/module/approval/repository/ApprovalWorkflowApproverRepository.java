package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowApproverEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalWorkflowApproverRepository extends JpaRepository<ApprovalWorkflowApproverEntity, Long> {
    List<ApprovalWorkflowApproverEntity> findByWorkflowLevel_IdAndIsActiveTrue(Long workflowLevelId);
}
