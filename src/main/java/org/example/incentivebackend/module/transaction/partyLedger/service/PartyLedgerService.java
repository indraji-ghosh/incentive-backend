package org.example.incentivebackend.module.transaction.partyLedger.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionCalculationService;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerEntryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartyLedgerService {

    private final PartyEntryRepository partyEntryRepository;
    private final CommissionPaymentRepository commissionPaymentRepository;
    private final CommissionCalculationService commissionCalculationService;

    @Transactional(readOnly = true)
    public PartyLedgerSummaryResponse getPartyLedgerSummary(Long partyId) {
        PartyEntryEntity party = partyEntryRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found"));

        BigDecimal totalCommissionEarned = commissionCalculationService.getTotalCommissionEarned(partyId);
        if (totalCommissionEarned == null) {
            totalCommissionEarned = BigDecimal.ZERO;
        }

        List<CommissionPaymentEntity> payments = commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE");
        
        BigDecimal totalCommissionPaid = payments.stream()
                .filter(p -> p.getPaymentAmount() != null)
                .map(CommissionPaymentEntity::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        PartyEntryEntity party = partyEntryRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found"));

        BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(partyId);
        if (totalEarned == null) {
            totalEarned = BigDecimal.ZERO;
        }

        List<CommissionPaymentEntity> payments = commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE");
        
        List<PartyLedgerEntryResponse> allEntries = new ArrayList<>();

        // Add the single Earned entry representing the total earned.
        // If there were individual entities, we would map them here. 
        // For now, we represent the total earned as an initial entry.
        if (totalEarned.compareTo(BigDecimal.ZERO) > 0) {
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

        // Sort chronologically. If dates are the same, rely on referenceNo to make it deterministic.
        allEntries.sort(Comparator.comparing(PartyLedgerEntryResponse::getLedgerDate)
                .thenComparing(PartyLedgerEntryResponse::getTransactionType)
                .thenComparing(PartyLedgerEntryResponse::getReferenceNo, Comparator.nullsLast(String::compareTo)));

        // Calculate running balance and totals for all entries (ignoring pagination so balance is accurate)
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal calculatedTotalDebit = BigDecimal.ZERO;
        BigDecimal calculatedTotalCredit = BigDecimal.ZERO;

        for (PartyLedgerEntryResponse entry : allEntries) {
            // balance = previous balance + credit - debit
            balance = balance.add(entry.getCredit()).subtract(entry.getDebit());
            entry.setBalance(balance);
            calculatedTotalDebit = calculatedTotalDebit.add(entry.getDebit());
            calculatedTotalCredit = calculatedTotalCredit.add(entry.getCredit());
        }

        // Apply filters
        List<PartyLedgerEntryResponse> filteredEntries = allEntries.stream()
                .filter(e -> {
                    if (filter.getFromDate() != null && e.getLedgerDate().isBefore(filter.getFromDate())) return false;
                    if (filter.getToDate() != null && e.getLedgerDate().isAfter(filter.getToDate())) return false;
                    if (filter.getTransactionType() != null && !filter.getTransactionType().isEmpty() && !e.getTransactionType().equalsIgnoreCase(filter.getTransactionType())) return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Apply pagination
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
