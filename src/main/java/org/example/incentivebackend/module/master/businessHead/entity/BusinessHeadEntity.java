package org.example.incentivebackend.module.master.businessHead.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.enums.StatusEnum;

@Auditable(
        module = AuditModule.MASTER,
        entity = "BusinessHead",
        table = "mm_buss_head"
)
@Entity
@Table(
        name = "mm_buss_head",
        indexes = {
                @Index(name = "IDX_MM_BUSS_HEAD_STATUS", columnList = "head_status")
        }
)
@Getter
@Setter
public class BusinessHeadEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "head_id", nullable = false, unique = true)
    private Long headId;

    @Column(name = "head_name", nullable = false, length = 100)
    private String headName;

    @Column(name = "head_short_code", nullable = false, length = 50, unique = true)
    private String headShortCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "head_status", nullable = false, length = 1)
    private StatusEnum headStatus;

}
