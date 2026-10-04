package org.example.incentivebackend.module.transaction.commissionpayment.repository;

import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CommissionPaymentRepository extends JpaRepository<CommissionPaymentEntity, Long> {

    List<CommissionPaymentEntity> findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(Long partyId, String status);

    List<CommissionPaymentEntity> findByParty_IdOrderByPaymentDateDescCommissionPaymentIdDesc(Long partyId);

    List<CommissionPaymentEntity> findByParty_IdAndStatusInOrderByPaymentDateDescCommissionPaymentIdDesc(Long partyId, List<String> statuses);

    org.springframework.data.domain.Page<CommissionPaymentEntity> findAllByOrderByPaymentDateDescCommissionPaymentIdDesc(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status IN ('PAID', 'ACTIVE') AND p.paymentType = 'PAYABLE_PAYMENT'")
    BigDecimal sumActivePayablePaymentAmountByPartyId(@Param("partyId") Long partyId);
    
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status IN ('PAID', 'ACTIVE') AND p.paymentType = 'PAYABLE_PAYMENT' AND p.commissionPaymentId != :excludeId")
    BigDecimal sumActivePayablePaymentAmountByPartyIdExcluding(@Param("partyId") Long partyId, @Param("excludeId") Long excludeId);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status IN ('PAID', 'ACTIVE') AND p.paymentType = 'ADVANCE_PAYMENT'")
    BigDecimal sumActiveAdvanceAmountByPartyId(@Param("partyId") Long partyId);

    @Query("SELECT COALESCE(SUM(p.adjustedAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status IN ('PAID', 'ACTIVE') AND p.paymentType = 'ADVANCE_PAYMENT'")
    BigDecimal sumAdjustedAdvanceAmountByPartyId(@Param("partyId") Long partyId);

    @Query("SELECT p FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status IN ('PAID', 'ACTIVE') AND p.paymentType = 'ADVANCE_PAYMENT' AND p.paymentAmount > p.adjustedAmount ORDER BY p.paymentDate ASC")
    List<CommissionPaymentEntity> findAvailableAdvancesForParty(@Param("partyId") Long partyId);
}
