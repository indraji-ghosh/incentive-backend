package org.example.incentivebackend.module.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.master.user.UserEntity;

@Entity
@Table(name = "tr_approval_workflow_approver", uniqueConstraints = {
        @UniqueConstraint(name = "UK_LEVEL_USER", columnNames = {"workflow_level_id", "user_id"})
})
@Getter
@Setter
public class ApprovalWorkflowApproverEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_level_id", nullable = false)
    private ApprovalWorkflowLevelEntity workflowLevel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
