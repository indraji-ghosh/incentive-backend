package org.example.incentivebackend.module.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.approval.enums.ApprovalActionType;
import org.example.incentivebackend.module.master.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tr_approval_action",
        indexes = {
                @Index(name = "IDX_APP_ACT_INST", columnList = "approval_instance_id")
        }
)
@Getter
@Setter
public class ApprovalActionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_instance_id", nullable = false)
    private ApprovalInstanceEntity approvalInstance;

    @Column(name = "level_number")
    private Integer levelNumber;

    @Column(name = "level_name", length = 100)
    private String levelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private ApprovalActionType action;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "action_by", nullable = false)
    private UserEntity actionBy;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;
}
