package org.example.incentivebackend.module.master.site.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;

@Entity
@Table(
        name = "mm_site",
        indexes = {
                @Index(
                        name = "IDX_MM_SITE_SITE_STATUS",
                        columnList = "site_status"
                )
        }
)
@Auditable(
        module = AuditModule.MASTER,
        entity = "Site",
        table = "mm_site"
)
@Getter
@Setter
public class SiteEntity extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "site_seq"
    )
    @SequenceGenerator(
            name = "site_seq",
            sequenceName = "mm_site_seq",
            allocationSize = 1
    )
    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(
            name = "site_name",
            nullable = false,
            length = 100
    )
    private String siteName;

    @Column(
            name = "site_short_code",
            nullable = false,
            length = 20
    )
    private String siteShortCode;

    @Column(
            name = "state",
            length = 100
    )
    private String state;



    @Enumerated(EnumType.STRING)
    @Column(
            name = "site_status",
            nullable = false,
            length = 20
    )
    private StatusEnum siteStatus;
}