package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalInstanceLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApprovalInstanceLevelRepository extends JpaRepository<ApprovalInstanceLevelEntity, Long> {
    List<ApprovalInstanceLevelEntity> findByApprovalInstance_IdOrderByLevelNumberAsc(Long approvalInstanceId);
    Optional<ApprovalInstanceLevelEntity> findByApprovalInstance_IdAndLevelNumber(Long approvalInstanceId, Integer levelNumber);
}
