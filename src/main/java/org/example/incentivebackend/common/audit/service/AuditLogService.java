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
            String oldValues,
            String newValues,
            String remarks,
            Long performedBy,
            String businessReference,
            String sourceModule,
            String sourceEntityType,
            Long sourceEntityId,
            String status
    ) {

        AuditLogEntity auditLog = AuditLogEntity.builder()
                .moduleName(moduleName)
                .entityName(entityName)
                .tableName(tableName)
                .entityId(entityId)
                .action(action)
                .oldValues(oldValues)
                .newValues(newValues)
                .data(newValues != null ? newValues : (oldValues != null ? oldValues : "{}"))
                .remarks(remarks)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .businessReference(businessReference)
                .sourceModule(sourceModule)
                .sourceEntityType(sourceEntityType)
                .sourceEntityId(sourceEntityId)
                .status(status)
                .build();

        auditLogRepository.save(auditLog);
    }

    // Overload for backward compatibility with older tests temporarily
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
        createAuditLog(
            moduleName, entityName, tableName, entityId, action,
            null, data, remarks, performedBy,
            null, null, null, null, "SUCCESS"
        );
    }
    
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditLogEntity> findAll(
            String moduleName, String entityName, Long entityId, 
            AuditAction action, Long performedBy, String businessReference,
            org.springframework.data.domain.Pageable pageable) {
        
        org.springframework.data.jpa.domain.Specification<AuditLogEntity> spec = (root, query, cb) -> cb.conjunction();
        
        if (moduleName != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("moduleName"), moduleName));
        }
        if (entityName != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityName"), entityName));
        }
        if (entityId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (performedBy != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("performedBy"), performedBy));
        }
        if (businessReference != null && !businessReference.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("businessReference")), "%" + businessReference.toLowerCase() + "%"));
        }
        
        return auditLogRepository.findAll(spec, pageable);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Optional<AuditLogEntity> findById(Long id) {
        return auditLogRepository.findById(id);
    }
}
