package org.example.incentivebackend.module.transaction.partypayable.service;

import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.partypayable.dto.request.PartyPayableFilter;
import org.example.incentivebackend.module.transaction.partypayable.dto.response.PartyPayableResponse;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PartyPayableService {

    List<PartyPayableEntity> generatePayablesForRake(RakeEntryEntity rake);

    List<PartyPayableEntity> generatePayablesForBill(BillEntity bill);

    List<PartyPayableEntity> generatePayablesForBillId(Long billId);

    List<PartyPayableResponse> generateAndGetResponsesForBillId(Long billId);

    List<PartyPayableEntity> createMonthlyFixedPayables(LocalDate monthDate);

    PartyPayableResponse findById(Long id);

    Page<PartyPayableResponse> findAll(PartyPayableFilter filter, Pageable pageable);

    List<PartyPayableResponse> findByParty(Long partyId);

    BigDecimal getTotalPayableEarned(Long partyId);
}
