package org.example.incentivebackend.module.transaction.bill.repository;

import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BillRepository
        extends JpaRepository<BillEntity, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<BillEntity> {

    boolean existsByBillNumberIgnoreCase(
            String billNumber
    );

    Optional<BillEntity> findByBillNumberIgnoreCase(
            String billNumber
    );

    List<BillEntity> findByParty_IdAndClient_ClientIdAndWorkingMonthBetween(Long partyId, Long clientId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    List<BillEntity> findByParty_IdAndWorkingMonthBetween(Long partyId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}