package org.example.incentivebackend.module.transaction.clientpayment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Auditable(
        module = AuditModule.TRANSACTION,
        entity = "ClientPayment",
        table = "txn_client_payment"
)
@Entity
@Table(
        name = "txn_client_payment",
        indexes = {
                @Index(
                        name = "IDX_TXN_CLIENT_PAYMENT_STATUS",
                        columnList = "payment_status"
                ),
                @Index(
                        name = "IDX_TXN_CLIENT_PAYMENT_BILL",
                        columnList = "bill_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_TXN_CLIENT_PAYMENT_NO",
                        columnNames = "payment_no"
                )
        }
)
@Getter
@Setter
public class ClientPaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "client_payment_seq"
    )
    @SequenceGenerator(
            name = "client_payment_seq",
            sequenceName = "txn_client_payment_seq",
            allocationSize = 1
    )
    @Column(name = "client_payment_id")
    private Long clientPaymentId;

    @Column(name = "payment_no", nullable = false, length = 50)
    private String paymentNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CLIENT_PAYMENT_BILL"))
    private BillEntity bill;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, foreignKey = @ForeignKey(name = "FK_CLIENT_PAYMENT_CLIENT"))
    private ClientEntity client;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private StatusEnum paymentStatus;
}
