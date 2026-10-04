package org.example.incentivebackend.common.audit.mapper;

import org.example.incentivebackend.common.audit.dto.response.AuditLogResponse;
import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .auditLogId(entity.getAuditLogId())
                .moduleName(entity.getModuleName())
                .entityName(entity.getEntityName())
                .tableName(entity.getTableName())
                .entityId(entity.getEntityId())
                .oldValues(entity.getOldValues())
                .newValues(entity.getNewValues())
                .action(entity.getAction())
                .remarks(entity.getRemarks())
                .performedBy(entity.getPerformedBy())
                .performedAt(entity.getPerformedAt())
                .businessReference(entity.getBusinessReference())
                .sourceModule(entity.getSourceModule())
                .sourceEntityType(entity.getSourceEntityType())
                .sourceEntityId(entity.getSourceEntityId())
                .status(entity.getStatus())
                .build();
    }

    public List<AuditLogResponse> toResponseList(List<AuditLogEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }
}
