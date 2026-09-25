package org.example.incentivebackend.module.transaction.partyLedger.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.PartyPaymentAdjustmentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionCalculationService;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerEntryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartyLedgerService {

    private final PartyRepository partyRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final PartyPaymentAdjustmentRepository partyPaymentAdjustmentRepository;
    private final CommissionCalculationService commissionCalculationService;
    private final PartyPayableRepository partyPayableRepository;

    @Transactional(readOnly = true)
    public PartyLedgerSummaryResponse getPartyLedgerSummary(Long partyId) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + partyId));

        BigDecimal totalCommissionEarned = commissionCalculationService.getTotalCommissionEarned(partyId);
        if (totalCommissionEarned == null) {
            totalCommissionEarned = BigDecimal.ZERO;
        }

        BigDecimal totalPayablePaid = commissionPaymentRepository.sumActivePayablePaymentAmountByPartyId(partyId);
        BigDecimal totalAdvanceAdjusted = commissionPaymentRepository.sumAdjustedAdvanceAmountByPartyId(partyId);
        if (totalPayablePaid == null) totalPayablePaid = BigDecimal.ZERO;
        if (totalAdvanceAdjusted == null) totalAdvanceAdjusted = BigDecimal.ZERO;
        BigDecimal totalCommissionPaid = totalPayablePaid.add(totalAdvanceAdjusted);

        BigDecimal outstandingCommission = totalCommissionEarned.subtract(totalCommissionPaid);

        return PartyLedgerSummaryResponse.builder()
                .partyId(party.getId())
                .partyName(party.getPartyName())
                .totalCommissionEarned(totalCommissionEarned)
                .totalCommissionPaid(totalCommissionPaid)
                .outstandingCommission(outstandingCommission)
                .build();
    }

    @Transactional(readOnly = true)
    public PartyLedgerResponse getPartyLedger(Long partyId, PartyLedgerFilter filter) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + partyId));

        BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(partyId);
        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }

        List<CommissionPaymentEntity> payments = commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE");
        List<PartyPayableEntity> payables = partyPayableRepository.findByParty_IdAndStatus(partyId, StatusEnum.A);
        List<PartyPaymentAdjustmentEntity> adjustments = partyPaymentAdjustmentRepository.findByAdvancePayment_Party_Id(partyId);
        
        List<PartyLedgerEntryResponse> allEntries = new ArrayList<>();

        if (!payables.isEmpty()) {
            for (PartyPayableEntity payable : payables) {
                allEntries.add(PartyLedgerEntryResponse.builder()
                        .ledgerDate(payable.getTransactionDate())
                        .transactionType("PARTY_PAYABLE")
                        .referenceNo(payable.getSourceReference() != null ? payable.getSourceReference() : payable.getSourceType() + "-" + payable.getSourceId())
                        .description((payable.getService() != null ? payable.getService().getName() : "Service") + " (" + payable.getCalculationBasis() + ")")
                        .debit(BigDecimal.ZERO)
                        .credit(payable.getPayableAmount())
                        .amount(payable.getPayableAmount())
                        .build());
            }
        } else if (totalEarned.compareTo(BigDecimal.ZERO) > 0) {
            allEntries.add(PartyLedgerEntryResponse.builder()
                    .ledgerDate(party.getCreatedAt() != null ? party.getCreatedAt().toLocalDate() : LocalDate.of(2000, 1, 1))
                    .transactionType("COMMISSION_EARNED")
                    .referenceNo("COMM-TOTAL")
                    .description("Total Commission Earned")
                    .debit(BigDecimal.ZERO)
                    .credit(totalEarned)
                    .amount(totalEarned)
                    .build());
        }

        for (CommissionPaymentEntity payment : payments) {
            if ("ADVANCE_PAYMENT".equals(payment.getPaymentType())) {
                allEntries.add(PartyLedgerEntryResponse.builder()
                        .ledgerDate(payment.getPaymentDate())
                        .transactionType("ADVANCE_PAYMENT")
                        .referenceNo(payment.getPaymentNo())
                        .description(payment.getRemarks() != null ? payment.getRemarks() : "Advance Payment")
                        .debit(BigDecimal.ZERO) // Does not reduce payable balance
                        .credit(BigDecimal.ZERO)
                        .amount(payment.getPaymentAmount())
                        .build());
            } else {
                allEntries.add(PartyLedgerEntryResponse.builder()
                        .ledgerDate(payment.getPaymentDate())
                        .transactionType("COMMISSION_PAYMENT")
                        .referenceNo(payment.getPaymentNo())
                        .description(payment.getRemarks() != null ? payment.getRemarks() : "Commission Payment")
                        .debit(payment.getPaymentAmount())
                        .credit(BigDecimal.ZERO)
                        .amount(payment.getPaymentAmount())
                        .build());
            }
        }

        for (PartyPaymentAdjustmentEntity adj : adjustments) {
            allEntries.add(PartyLedgerEntryResponse.builder()
                    .ledgerDate(adj.getCreatedAt() != null ? adj.getCreatedAt().toLocalDate() : LocalDate.now())
                    .transactionType("ADVANCE_ADJUSTMENT")
                    .referenceNo("ADJ-" + adj.getAdjustmentId())
                    .description("Advance Adjusted against Payable #" + adj.getPayable().getId())
                    .debit(adj.getAdjustedAmount()) // This reduces the payable balance
                    .credit(BigDecimal.ZERO)
                    .amount(adj.getAdjustedAmount())
                    .build());
        }

        // Sort chronologically
        allEntries.sort(Comparator.comparing(PartyLedgerEntryResponse::getLedgerDate)
                .thenComparing(PartyLedgerEntryResponse::getTransactionType)
                .thenComparing(PartyLedgerEntryResponse::getReferenceNo, Comparator.nullsLast(String::compareTo)));

        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal calculatedTotalDebit = BigDecimal.ZERO;
        BigDecimal calculatedTotalCredit = BigDecimal.ZERO;

        for (PartyLedgerEntryResponse entry : allEntries) {
            balance = balance.add(entry.getCredit()).subtract(entry.getDebit());
            entry.setBalance(balance);
            calculatedTotalDebit = calculatedTotalDebit.add(entry.getDebit());
            calculatedTotalCredit = calculatedTotalCredit.add(entry.getCredit());
        }

        List<PartyLedgerEntryResponse> filteredEntries = allEntries.stream()
                .filter(e -> {
                    if (filter.getFromDate() != null && e.getLedgerDate().isBefore(filter.getFromDate())) return false;
                    if (filter.getToDate() != null && e.getLedgerDate().isAfter(filter.getToDate())) return false;
                    if (filter.getTransactionType() != null && !filter.getTransactionType().isEmpty() && !e.getTransactionType().equalsIgnoreCase(filter.getTransactionType())) return false;
                    return true;
                })
                .collect(Collectors.toList());

        int page = filter.getPage() >= 0 ? filter.getPage() : 0;
        int size = filter.getSize() > 0 ? filter.getSize() : 10;
        
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, filteredEntries.size());
        
        List<PartyLedgerEntryResponse> pagedEntries = new ArrayList<>();
        if (startIndex < filteredEntries.size()) {
            pagedEntries = filteredEntries.subList(startIndex, endIndex);
        }

        return PartyLedgerResponse.builder()
                .partyId(party.getId())
                .partyName(party.getPartyName())
                .totalCommissionEarned(calculatedTotalCredit)
                .totalCommissionPaid(calculatedTotalDebit)
                .outstandingCommission(calculatedTotalCredit.subtract(calculatedTotalDebit))
                .entries(pagedEntries)
                .build();
    }
}
