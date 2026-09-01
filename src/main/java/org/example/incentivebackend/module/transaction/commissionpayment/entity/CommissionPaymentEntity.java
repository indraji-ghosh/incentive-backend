package org.example.incentivebackend.module.transaction.commissionpayment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tr_commission_payment")
@Getter
@Setter
@Auditable(module = AuditModule.TRANSACTION, entity = "Commission Payment", table = "tr_commission_payment")
public class CommissionPaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commission_payment_id")
    private Long commissionPaymentId;

    @Column(name = "payment_no", nullable = false, unique = true, length = 50)
    private String paymentNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private PartyEntryEntity party;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status;
}
