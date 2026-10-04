package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalInstanceApproverEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalInstanceApproverRepository extends JpaRepository<ApprovalInstanceApproverEntity, Long> {
    List<ApprovalInstanceApproverEntity> findByInstanceLevel_Id(Long instanceLevelId);
    boolean existsByInstanceLevel_IdAndUser_UserId(Long instanceLevelId, Long userId);
}
