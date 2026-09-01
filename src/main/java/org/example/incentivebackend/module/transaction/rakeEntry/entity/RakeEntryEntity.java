package org.example.incentivebackend.module.transaction.rakeEntry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tx_rake_entry",
        indexes = {
                @Index(
                        name = "IDX_TX_RAKE_ENTRY_CLIENT",
                        columnList = "client_id"
                ),
                @Index(
                        name = "IDX_TX_RAKE_ENTRY_PARTY",
                        columnList = "party_id"
                ),
                @Index(
                        name = "IDX_TX_RAKE_ENTRY_WORKING_MONTH",
                        columnList = "working_month"
                )
        }
)
@Getter
@Setter
public class RakeEntryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rake_entry_id")
    private Long rakeEntryId;

    @Column(
            name = "working_month",
            nullable = false
    )
    private LocalDate workingMonth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "client_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TX_RAKE_ENTRY_CLIENT"
            )
    )
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "party_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TX_RAKE_ENTRY_PARTY"
            )
    )
    private PartyEntryEntity party;

    @Column(
            name = "remarks",
            length = 500
    )
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "rake_status", length = 1)
    private StatusEnum rakeStatus;

    @OneToMany(
            mappedBy = "rakeEntry",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RakeAnnexureEntity> annexures =
            new ArrayList<>();
}
