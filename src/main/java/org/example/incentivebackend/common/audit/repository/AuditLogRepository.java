package org.example.incentivebackend.common.audit.repository;

import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {


}
