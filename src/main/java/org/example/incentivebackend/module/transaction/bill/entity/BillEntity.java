package org.example.incentivebackend.module.transaction.bill.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tx_bill",
        indexes = {
                @Index(
                        name = "IDX_TX_BILL_CLIENT",
                        columnList = "client_id"
                ),
                @Index(
                        name = "IDX_TX_BILL_PARTY",
                        columnList = "party_id"
                ),
                @Index(
                        name = "IDX_TX_BILL_WORKING_MONTH",
                        columnList = "working_month"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TX_BILL_NUMBER",
                        columnNames = "bill_number"
                )
        }
)
@Getter
@Setter
public class BillEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    @Column(
            name = "bill_number",
            nullable = false,
            length = 50
    )
    private String billNumber;

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
                    name = "FK_TX_BILL_CLIENT"
            )
    )
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "party_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TX_BILL_PARTY"
            )
    )
    private PartyEntryEntity party;



    @Column(
            name = "bill_amount",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal billAmount;

    @Column(
            name = "remarks",
            length = 500
    )
    private String remarks;

    @OneToMany(
            mappedBy = "bill",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BillAnnexureEntity> annexures =
            new ArrayList<>();
}