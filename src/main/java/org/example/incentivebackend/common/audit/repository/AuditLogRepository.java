package org.example.incentivebackend.common.audit.repository;

import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {


}
