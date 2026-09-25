package org.example.incentivebackend.module.association.partyassignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "tr_party_service_config",
        indexes = {
                @Index(name = "IDX_PSC_ASSIGNMENT", columnList = "party_assignment_id"),
                @Index(name = "IDX_PSC_SERVICE", columnList = "service_type_id"),
                @Index(name = "IDX_PSC_PAYMENT_TYPE", columnList = "payment_type_id"),
                @Index(name = "IDX_PSC_EFFECTIVE_FROM", columnList = "effective_from"),
                @Index(name = "IDX_PSC_EFFECTIVE_TO", columnList = "effective_to")
        }
)
@Getter
@Setter
public class PartyServiceConfigurationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_assignment_id", nullable = false)
    private PartyAssignmentEntity partyAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_type_id", nullable = false)
    private ServiceTypeEntity service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentTypeEntity paymentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private UnitEntity unit;

    @Column(name = "rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEnum status = StatusEnum.A;

    @Column(name = "notes", length = 500)
    private String notes;
}
