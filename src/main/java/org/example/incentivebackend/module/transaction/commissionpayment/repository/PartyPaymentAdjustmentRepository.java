package org.example.incentivebackend.module.transaction.commissionpayment.repository;

import org.example.incentivebackend.module.transaction.commissionpayment.entity.PartyPaymentAdjustmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyPaymentAdjustmentRepository extends JpaRepository<PartyPaymentAdjustmentEntity, Long> {
    List<PartyPaymentAdjustmentEntity> findByCommissionPayment_Party_Id(Long partyId);
    List<PartyPaymentAdjustmentEntity> findByCommissionPayment_CommissionPaymentId(Long commissionPaymentId);
    List<PartyPaymentAdjustmentEntity> findByPayable_Id(Long payableId);
}
