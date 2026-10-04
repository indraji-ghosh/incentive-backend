package org.example.incentivebackend.module.approval.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.master.user.UserEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tr_approval_instance_level",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_INSTANCE_LEVEL", columnNames = {"approval_instance_id", "level_number"})
        }
)
@Getter
@Setter
public class ApprovalInstanceLevelEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approval_instance_id", nullable = false)
    private ApprovalInstanceEntity approvalInstance;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber;

    @Column(name = "level_name", nullable = false, length = 100)
    private String levelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ApprovalStatus status;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "completed_by")
    private UserEntity completedBy;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "instanceLevel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ApprovalInstanceApproverEntity> approvers = new ArrayList<>();
}
