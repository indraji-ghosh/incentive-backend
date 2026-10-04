package org.example.incentivebackend.module.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tr_approval_workflow", uniqueConstraints = {
        @UniqueConstraint(name = "UK_WORKFLOW_ENTITY_NAME", columnNames = {"entity_type", "workflow_name"})
})
@Getter
@Setter
public class ApprovalWorkflowEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "workflow_name", nullable = false, length = 100)
    private String workflowName;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private WorkflowEntityType entityType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "require_maker_checker", nullable = false)
    private Boolean requireMakerChecker = true;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC, levelNumber ASC")
    private List<ApprovalWorkflowLevelEntity> levels = new ArrayList<>();
}
