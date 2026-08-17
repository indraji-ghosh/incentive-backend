package org.example.incentivebackend.module.association.clientSite.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;

@Entity
@Table(
        name = "mm_client_site",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_MM_CLIENT_SITE",
                        columnNames = {
                                "client_id",
                                "site_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "IDX_CLIENT_SITE_CLIENT",
                        columnList = "client_id"
                ),
                @Index(
                        name = "IDX_CLIENT_SITE_SITE",
                        columnList = "site_id"
                )
        }
)
@Getter
@Setter
public class ClientSiteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_site_id")
    private Long clientSiteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "client_id",
            nullable = false
    )
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "site_id",
            nullable = false
    )
    private SiteEntity site;
}