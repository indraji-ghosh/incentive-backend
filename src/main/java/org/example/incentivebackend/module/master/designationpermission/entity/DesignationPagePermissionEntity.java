package org.example.incentivebackend.module.master.designationpermission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.page.entity.PageEntity;

@Entity
@Table(
    name = "mm_designation_page_permission",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"designation_id", "page_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationPagePermissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "desig_perm_seq")
    @SequenceGenerator(name = "desig_perm_seq", sequenceName = "mm_designation_page_perm_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", nullable = false)
    private DesignationEntity designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageEntity page;

    @Column(name = "can_view", nullable = false)
    @Builder.Default
    private Boolean canView = false;

    @Column(name = "can_create", nullable = false)
    @Builder.Default
    private Boolean canCreate = false;

    @Column(name = "can_edit", nullable = false)
    @Builder.Default
    private Boolean canEdit = false;

    @Column(name = "can_delete", nullable = false)
    @Builder.Default
    private Boolean canDelete = false;

    @Column(name = "can_export", nullable = false)
    @Builder.Default
    private Boolean canExport = false;

    @Column(name = "can_submit", nullable = false)
    @Builder.Default
    private Boolean canSubmit = false;

    @Column(name = "can_approve", nullable = false)
    @Builder.Default
    private Boolean canApprove = false;

    @Column(name = "can_reject", nullable = false)
    @Builder.Default
    private Boolean canReject = false;
}

