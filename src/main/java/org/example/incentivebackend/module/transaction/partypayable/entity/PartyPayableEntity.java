package org.example.incentivebackend.module.transaction.partypayable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.audit.anotation.Auditable;
import org.example.incentivebackend.common.audit.enums.AuditModule;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.common.enums.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "tx_party_payable",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PAYABLE_SOURCE_ASSIGN_SERVICE",
                        columnNames = {"source_type", "source_id", "party_assignment_id", "service_type_id"}
                )
        },
        indexes = {
                @Index(name = "IDX_PAYABLE_PARTY", columnList = "party_id"),
                @Index(name = "IDX_PAYABLE_ASSIGNMENT", columnList = "party_assignment_id"),
                @Index(name = "IDX_PAYABLE_DATE", columnList = "transaction_date"),
                @Index(name = "IDX_PAYABLE_SOURCE", columnList = "source_type, source_id"),
                @Index(name = "IDX_PAYABLE_STATUS", columnList = "status")
        }
)
@Auditable(
        module = AuditModule.TRANSACTION,
        entity = "PartyPayable",
        table = "tx_party_payable"
)
@Getter
@Setter
public class PartyPayableEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private PartyEntity party;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_assignment_id", nullable = false)
    private PartyAssignmentEntity partyAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceTypeEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_service_config_id")
    private PartyServiceConfigurationEntity partyServiceConfiguration;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType; // "RAKE", "BILL", "MONTHLY_FIXED"

    @Column(name = "source_id", nullable = false, length = 100)
    private String sourceId; // e.g. "1001", "2026-09"

    @Column(name = "source_reference", length = 150)
    private String sourceReference; // e.g. RR No, Bill No, "MONTHLY-2026-09"

    @Column(name = "calculation_basis", nullable = false, length = 50)
    private String calculationBasis; // "RAKE_BASED", "WAGON_BASED", "MT_BASED", "MONTHLY_FIXED", "BILL_BASED"

    @Column(name = "quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    @Column(name = "rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "payable_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal payableAmount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEnum status = StatusEnum.A;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatusEnum paymentStatus = PaymentStatusEnum.UNPAID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_payment_id")
    private CommissionPaymentEntity commissionPayment;
}
