package org.example.incentivebackend.module.transaction.rakeEntry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "tx_rake_annexure",
        indexes = {
                @Index(
                        name = "IDX_TX_RAKE_ANNEXURE_RAKE",
                        columnList = "rake_entry_id"
                )
        }
)
@Getter
@Setter
public class RakeAnnexureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rake_annexure_id")
    private Long rakeAnnexureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "rake_entry_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TX_RAKE_ANNEXURE_RAKE"
            )
    )
    private RakeEntryEntity rakeEntry;

    @Column(
            name = "rr_no",
            nullable = false,
            length = 100
    )
    private String rrNo;

    @Column(name = "rr_date", nullable = false)
    private LocalDate rrDate;

    @Column(
            name = "challan",
            length = 100
    )
    private String challan;

    @Column(name = "load_date")
    private LocalDate loadDate;

    @Column(
            name = "siding",
            length = 150
    )
    private String siding;

    @Column(
            name = "destination",
            length = 150
    )
    private String destination;

    @Column(
            name = "wagons"
    )
    private Integer wagons;

    @Column(
            name = "weight",
            precision = 18,
            scale = 2
    )
    private BigDecimal weight;

    @Column(
            name = "quantity",
            precision = 18,
            scale = 2
    )
    private BigDecimal quantity;

    @Column(
            name = "annexure_amount",
            precision = 18,
            scale = 2
    )
    private BigDecimal annexureAmount;

    @Column(
            name = "notes",
            length = 500
    )
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 1)
    private StatusEnum status;
}
