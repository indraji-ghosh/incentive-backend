package org.example.incentivebackend.module.transaction.clientpayment.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.transaction.clientpayment.entity.ClientPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ClientPaymentRepository extends JpaRepository<ClientPaymentEntity, Long> {

    @Query("SELECT p FROM ClientPaymentEntity p WHERE p.bill.billId = :billId AND p.paymentStatus = :status ORDER BY p.paymentDate DESC")
    List<ClientPaymentEntity> findActivePaymentsByBillId(@Param("billId") Long billId, @Param("status") StatusEnum status);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM ClientPaymentEntity p WHERE p.bill.billId = :billId AND p.paymentStatus = :status")
    BigDecimal getTotalPaidAmountByBillId(@Param("billId") Long billId, @Param("status") StatusEnum status);

    @Query("SELECT COALESCE(SUM(p.paymentAmount), 0) FROM ClientPaymentEntity p WHERE p.bill.billId = :billId AND p.paymentStatus = :status AND p.clientPaymentId != :excludePaymentId")
    BigDecimal getTotalPaidAmountByBillIdExcluding(@Param("billId") Long billId, @Param("excludePaymentId") Long excludePaymentId, @Param("status") StatusEnum status);

    boolean existsByPaymentNoIgnoreCase(String paymentNo);

    List<ClientPaymentEntity> findByClientClientIdAndPaymentDateBetween(Long clientId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    List<ClientPaymentEntity> findByClientClientIdInAndPaymentDateBetween(List<Long> clientIds, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
