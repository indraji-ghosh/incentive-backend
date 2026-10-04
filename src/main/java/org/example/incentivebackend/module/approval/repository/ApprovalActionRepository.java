package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalActionRepository extends JpaRepository<ApprovalActionEntity, Long> {
    List<ApprovalActionEntity> findByApprovalInstance_IdOrderByActionAtAscIdAsc(Long approvalInstanceId);
}
