package org.example.incentivebackend.module.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tr_approval_workflow_level", uniqueConstraints = {
        @UniqueConstraint(name = "UK_WORKFLOW_LEVEL", columnNames = {"workflow_id", "level_number"})
})
@Getter
@Setter
public class ApprovalWorkflowLevelEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_id", nullable = false)
    private ApprovalWorkflowEntity workflow;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "workflowLevel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApprovalWorkflowApproverEntity> approvers = new ArrayList<>();
}
