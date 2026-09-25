package org.example.incentivebackend.module.association.partyassignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tr_party_assignment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PARTY_CLIENT_SITE",
                        columnNames = {"party_id", "client_id", "site_id"}
                )
        },
        indexes = {
                @Index(name = "IDX_PARTY_ASSIGN_CLIENT", columnList = "client_id"),
                @Index(name = "IDX_PARTY_ASSIGN_SITE", columnList = "site_id"),
                @Index(name = "IDX_PARTY_ASSIGN_PARTY", columnList = "party_id"),
                @Index(name = "IDX_PARTY_ASSIGN_STATUS", columnList = "status")
        }
)
@Auditable(
        module = AuditModule.ASSOCIATION,
        entity = "PartyAssignment",
        table = "tr_party_assignment"
)
@Getter
@Setter
public class PartyAssignmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private PartyEntity party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEnum status = StatusEnum.A;

    @OneToMany(
            mappedBy = "partyAssignment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PartyServiceConfigurationEntity> serviceConfigurations = new ArrayList<>();

    public void addServiceConfiguration(PartyServiceConfigurationEntity configuration) {
        serviceConfigurations.add(configuration);
        configuration.setPartyAssignment(this);
    }

    public void removeServiceConfiguration(PartyServiceConfigurationEntity configuration) {
        serviceConfigurations.remove(configuration);
        configuration.setPartyAssignment(null);
    }
}
