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
import java.util.Map;
import java.util.HashMap;

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

    private boolean isClientSiteMatch(PartyPayableEntity payable, PartyLedgerFilter filter) {
        if (filter.getClientId() != null && !payable.getPartyAssignment().getClient().getClientId().equals(filter.getClientId())) {
            return false;
        }
        if (filter.getSiteId() != null && !payable.getPartyAssignment().getSite().getSiteId().equals(filter.getSiteId())) {
            return false;
        }
        return true;
    }

    @Transactional(readOnly = true)
    public PartyLedgerResponse getPartyLedger(Long partyId, PartyLedgerFilter filter) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + partyId));

        List<CommissionPaymentEntity> payments = commissionPaymentRepository.findByParty_IdAndStatusInOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, List.of("PAID", "ACTIVE"));
        List<PartyPayableEntity> payables = partyPayableRepository.findByParty_IdAndStatus(partyId, StatusEnum.A);
        List<PartyPaymentAdjustmentEntity> adjustments = partyPaymentAdjustmentRepository.findByCommissionPayment_Party_Id(partyId);
        
        List<PartyLedgerEntryResponse> allEntries = new ArrayList<>();

        BigDecimal calculatedTotalEarned = BigDecimal.ZERO;
        BigDecimal calculatedTotalPaid = BigDecimal.ZERO;

        for (PartyPayableEntity payable : payables) {
            if (isClientSiteMatch(payable, filter)) {
                allEntries.add(PartyLedgerEntryResponse.builder()
                        .ledgerDate(payable.getTransactionDate())
                        .transactionType("PARTY_PAYABLE")
                        .referenceNo(payable.getSourceReference() != null ? payable.getSourceReference() : payable.getSourceType() + "-" + payable.getSourceId())
                        .clientName(payable.getPartyAssignment().getClient().getClientName())
                        .siteName(payable.getPartyAssignment().getSite().getSiteName())
                        .description((payable.getService() != null ? payable.getService().getName() : "Service") + " (" + payable.getCalculationBasis() + ")")
                        .debit(BigDecimal.ZERO)
                        .credit(payable.getPayableAmount())
                        .amount(payable.getPayableAmount())
                        .build());
                calculatedTotalEarned = calculatedTotalEarned.add(payable.getPayableAmount());
            }
        }

        for (CommissionPaymentEntity payment : payments) {
            if ("ADVANCE_PAYMENT".equals(payment.getPaymentType())) {
                if (filter.getClientId() == null) {
                    allEntries.add(PartyLedgerEntryResponse.builder()
                            .ledgerDate(payment.getPaymentDate())
                            .transactionType("ADVANCE_PAYMENT")
                            .referenceNo(payment.getPaymentNo())
                            .description(payment.getRemarks() != null ? payment.getRemarks() : "Advance Payment")
                            .debit(BigDecimal.ZERO)
                            .credit(BigDecimal.ZERO)
                            .amount(payment.getPaymentAmount())
                            .build());
                }
            } else {
                List<PartyPaymentAdjustmentEntity> allocs = adjustments.stream()
                        .filter(a -> a.getCommissionPayment().getCommissionPaymentId().equals(payment.getCommissionPaymentId()))
                        .filter(a -> isClientSiteMatch(a.getPayable(), filter))
                        .collect(Collectors.toList());
                
                if (!allocs.isEmpty()) {
                    BigDecimal allocatedAmount = allocs.stream().map(PartyPaymentAdjustmentEntity::getAdjustedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    calculatedTotalPaid = calculatedTotalPaid.add(allocatedAmount);
                    
                    String desc = "Against: " + allocs.stream()
                            .map(a -> (a.getPayable().getSourceReference() != null ? a.getPayable().getSourceReference() : a.getPayable().getSourceId()) + " -> " + a.getAdjustedAmount())
                            .collect(Collectors.joining(", "));

                    allEntries.add(PartyLedgerEntryResponse.builder()
                            .ledgerDate(payment.getPaymentDate())
                            .transactionType("COMMISSION_PAYMENT")
                            .referenceNo(payment.getPaymentNo())
                            .clientName(allocs.get(0).getPayable().getPartyAssignment().getClient().getClientName())
                            .siteName(allocs.get(0).getPayable().getPartyAssignment().getSite().getSiteName())
                            .description(desc)
                            .debit(allocatedAmount)
                            .credit(BigDecimal.ZERO)
                            .amount(allocatedAmount)
                            .build());
                } else if (filter.getClientId() == null) {
                    allEntries.add(PartyLedgerEntryResponse.builder()
                            .ledgerDate(payment.getPaymentDate())
                            .transactionType("COMMISSION_PAYMENT")
                            .referenceNo(payment.getPaymentNo())
                            .description(payment.getRemarks() != null ? payment.getRemarks() : "Unallocated Commission Payment")
                            .debit(payment.getPaymentAmount())
                            .credit(BigDecimal.ZERO)
                            .amount(payment.getPaymentAmount())
                            .build());
                    calculatedTotalPaid = calculatedTotalPaid.add(payment.getPaymentAmount());
                }
            }
        }

        for (PartyPaymentAdjustmentEntity adj : adjustments) {
            if (adj.getCommissionPayment() != null && "ADVANCE_PAYMENT".equals(adj.getCommissionPayment().getPaymentType())) {
                if (isClientSiteMatch(adj.getPayable(), filter)) {
                    allEntries.add(PartyLedgerEntryResponse.builder()
                            .ledgerDate(adj.getCreatedAt() != null ? adj.getCreatedAt().toLocalDate() : LocalDate.now())
                            .transactionType("ADVANCE_ADJUSTMENT")
                            .referenceNo("ADJ-" + adj.getAdjustmentId())
                            .clientName(adj.getPayable().getPartyAssignment().getClient().getClientName())
                            .siteName(adj.getPayable().getPartyAssignment().getSite().getSiteName())
                            .description("Advance Adjusted against Payable #" + adj.getPayable().getId())
                            .debit(adj.getAdjustedAmount())
                            .credit(BigDecimal.ZERO)
                            .amount(adj.getAdjustedAmount())
                            .build());
                    calculatedTotalPaid = calculatedTotalPaid.add(adj.getAdjustedAmount());
                }
            }
        }

        allEntries.sort(Comparator.comparing(PartyLedgerEntryResponse::getLedgerDate)
                .thenComparing(PartyLedgerEntryResponse::getTransactionType)
                .thenComparing(PartyLedgerEntryResponse::getReferenceNo, Comparator.nullsLast(String::compareTo)));

        BigDecimal balance = BigDecimal.ZERO;

        for (PartyLedgerEntryResponse entry : allEntries) {
            balance = balance.add(entry.getCredit()).subtract(entry.getDebit());
            entry.setBalance(balance);
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
                .totalCommissionEarned(calculatedTotalEarned)
                .totalCommissionPaid(calculatedTotalPaid)
                .outstandingCommission(calculatedTotalEarned.subtract(calculatedTotalPaid))
                .entries(pagedEntries)
                .build();
    }
}
