package org.example.incentivebackend.common.audit.service;

import lombok.*;
import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.example.incentivebackend.common.audit.enums.AuditAction;
import org.example.incentivebackend.common.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void createAuditLog(
            String moduleName,
            String entityName,
            String tableName,
            Long entityId,
            AuditAction action,
            String data,
            String remarks,
            Long performedBy
    ) {

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .moduleName(moduleName)
                .entityName(entityName)
                .tableName(tableName)
                .entityId(entityId)
                .action(action)
                .data(data)
                .remarks(remarks)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }
}
