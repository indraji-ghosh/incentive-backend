package org.example.incentivebackend.common.audit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.incentivebackend.common.audit.enums.AuditAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long auditLogId;
    private String moduleName;
    private String entityName;
    private String tableName;
    private Long entityId;
    
    private String oldValues;
    private String newValues;
    
    private AuditAction action;
    private String remarks;
    private Long performedBy;
    private LocalDateTime performedAt;

    private String businessReference;
    private String sourceModule;
    private String sourceEntityType;
    private Long sourceEntityId;
    private String status;
}
