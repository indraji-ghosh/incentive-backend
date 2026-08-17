package org.example.incentivebackend.common.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.incentivebackend.common.audit.enums.AuditAction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cm_audit_log",
        indexes = {
                @Index(name = "idx_audit_module", columnList = "module_name"),
                @Index(name = "idx_audit_entity", columnList = "entity_name"),
                @Index(name = "idx_audit_entity_id", columnList = "entity_id"),
                @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
                @Index(name = "idx_audit_performed_at", columnList = "performed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "audit_log_seq"
    )
    @SequenceGenerator(
            name = "audit_log_seq",
            sequenceName = "cm_audit_log_seq",
            allocationSize = 1
    )
    @Column(name = "audit_log_id")
    private Long auditLogId;

    /**
     * ERP module.
     *
     * Example:
     * MASTER
     * PROPERTY
     * PAYMENT
     * USER
     */
    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    /**
     * Business entity name.
     *
     * Example:
     * BusinessHead
     * Company
     * User
     */
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    /**
     * Actual database table.
     *
     * Example:
     * mm_buss_head
     * mm_company
     * mm_user
     */
    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    /**
     * Primary key of the actual entity.
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * CREATE -> Full object
     * UPDATE -> Changed fields only
     * DELETE -> Deleted object / previous values
     */
    @Lob
    @Column(name = "data", nullable = false)
    private String data;

    /**
     * CREATE / UPDATE / DELETE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private AuditAction action;

    /**
     * Optional explanation for the action.
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    /**
     * ID of the user who performed the operation.
     */
    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    /**
     * Date and time of the operation.
     */
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;
}