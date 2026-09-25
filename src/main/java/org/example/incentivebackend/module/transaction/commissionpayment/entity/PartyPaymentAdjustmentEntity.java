package org.example.incentivebackend.module.transaction.commissionpayment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "tr_party_payment_adjustment")
@Getter
@Setter
@Auditable(module = AuditModule.TRANSACTION, entity = "Party Payment Adjustment", table = "tr_party_payment_adjustment")
public class PartyPaymentAdjustmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_id")
    private Long adjustmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_payment_id", nullable = false)
    private CommissionPaymentEntity advancePayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_payable_id", nullable = false)
    private PartyPayableEntity payable;

    @Column(name = "adjusted_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustedAmount;
}
