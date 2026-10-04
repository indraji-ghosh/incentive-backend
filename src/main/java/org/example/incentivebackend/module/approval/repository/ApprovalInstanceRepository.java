package org.example.incentivebackend.module.approval.repository;

import org.example.incentivebackend.module.approval.entity.ApprovalInstanceEntity;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstanceEntity, Long> {
    Optional<ApprovalInstanceEntity> findTopByEntityTypeAndEntityIdOrderByAttemptNumberDesc(
            WorkflowEntityType entityType, Long entityId);

    Optional<ApprovalInstanceEntity> findByEntityTypeAndEntityIdAndStatus(
            WorkflowEntityType entityType, Long entityId, ApprovalStatus status);

    @Query("SELECT i FROM ApprovalInstanceEntity i WHERE i.entityType = :entityType AND i.entityId IN :entityIds AND i.status = 'IN_PROGRESS'")
    List<ApprovalInstanceEntity> findActiveInstancesForEntities(
            @Param("entityType") WorkflowEntityType entityType,
            @Param("entityIds") List<Long> entityIds);
}
