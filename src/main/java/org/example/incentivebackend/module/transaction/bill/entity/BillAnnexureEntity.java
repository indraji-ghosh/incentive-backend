package org.example.incentivebackend.module.transaction.bill.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "tx_bill_annexure",
        indexes = {
                @Index(
                        name = "IDX_TX_BILL_ANNEXURE_BILL",
                        columnList = "bill_id"
                )
        }
)
@Getter
@Setter
public class BillAnnexureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_annexure_id")
    private Long billAnnexureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "bill_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TX_BILL_ANNEXURE_BILL"
            )
    )
    private BillEntity bill;

    @Column(
            name = "rr_no",
            nullable = false,
            length = 100
    )
    private String rrNo;

    @Column(name = "rr_date")
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
            name = "wagons",
            nullable = false
    )
    private Integer wagons;
}