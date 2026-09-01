package org.example.incentivebackend.module.transaction.commissionpayment.repository;

import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CommissionPaymentRepository extends JpaRepository<CommissionPaymentEntity, Long> {

    List<CommissionPaymentEntity> findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(Long partyId, String status);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status = 'ACTIVE'")
    BigDecimal sumActivePaymentAmountByPartyId(@Param("partyId") Long partyId);
    
    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM CommissionPaymentEntity p WHERE p.party.id = :partyId AND p.status = 'ACTIVE' AND p.commissionPaymentId != :excludeId")
    BigDecimal sumActivePaymentAmountByPartyIdExcluding(@Param("partyId") Long partyId, @Param("excludeId") Long excludeId);
}
