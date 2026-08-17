package org.example.incentivebackend.module.transaction.bill.repository;

import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillRepository
        extends JpaRepository<BillEntity, Long> {

    boolean existsByBillNumberIgnoreCase(
            String billNumber
    );

    Optional<BillEntity> findByBillNumberIgnoreCase(
            String billNumber
    );
}