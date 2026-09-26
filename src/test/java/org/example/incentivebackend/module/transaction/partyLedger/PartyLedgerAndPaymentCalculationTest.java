package org.example.incentivebackend.module.transaction.partyLedger;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionCalculationService;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerEntryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.service.PartyLedgerService;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyLedgerAndPaymentCalculationTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private CommissionPaymentRepository commissionPaymentRepository;

    @Mock
    private CommissionCalculationService commissionCalculationService;

    @Mock
    private PartyPayableRepository partyPayableRepository;

    @Mock
    private org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository partyPaymentAdjustmentRepository;

    @InjectMocks
    private PartyLedgerService partyLedgerService;

    private PartyEntity party;
    private ServiceTypeEntity service;

    @BeforeEach
    void setUp() {
        party = new PartyEntity();
        party.setId(1L);
        party.setPartyName("Vendor Logistics");
        party.setCreatedAt(LocalDateTime.of(2026, 7, 1, 10, 0));

        service = new ServiceTypeEntity();
        service.setId(10L);
        service.setName("Covering");
    }

    @Test
    @DisplayName("Requirements 16 & 17: Partial Party Payment and Outstanding calculation")
    void testPartialPartyPaymentAndOutstandingCalculation() {
        // Party Payable = 50,000
        PartyPayableEntity payable = new PartyPayableEntity();
        payable.setId(100L);
        payable.setPayableAmount(new BigDecimal("50000.00"));
        payable.setTransactionDate(LocalDate.of(2026, 8, 1));
        payable.setStatus(StatusEnum.A);
        payable.setService(service);
        payable.setCalculationBasis("MONTHLY_FIXED");
        payable.setSourceReference("MF-2026-08");

        // Payment 1 = 20,000
        CommissionPaymentEntity payment1 = new CommissionPaymentEntity();
        payment1.setCommissionPaymentId(1L);
        payment1.setPaymentNo("PAY-001");
        payment1.setPaymentAmount(new BigDecimal("20000.00"));
        payment1.setPaymentDate(LocalDate.of(2026, 8, 10));
        payment1.setStatus("ACTIVE");

        // Payment 2 = 15,000
        CommissionPaymentEntity payment2 = new CommissionPaymentEntity();
        payment2.setCommissionPaymentId(2L);
        payment2.setPaymentNo("PAY-002");
        payment2.setPaymentAmount(new BigDecimal("15000.00"));
        payment2.setPaymentDate(LocalDate.of(2026, 8, 20));
        payment2.setStatus("ACTIVE");

        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("50000.00"));
        org.mockito.Mockito.lenient().when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE"))
                .thenReturn(List.of(payment1, payment2));
        org.mockito.Mockito.lenient().when(commissionPaymentRepository.sumActivePayablePaymentAmountByPartyId(1L))
                .thenReturn(new BigDecimal("35000.00"));
        org.mockito.Mockito.lenient().when(commissionPaymentRepository.sumAdjustedAdvanceAmountByPartyId(1L))
                .thenReturn(BigDecimal.ZERO);
        org.mockito.Mockito.lenient().when(partyPaymentAdjustmentRepository.findByCommissionPayment_Party_Id(1L))
                .thenReturn(java.util.Collections.emptyList());

        // Test Summary: Outstanding = Total Payable (50,000) - Total Paid (35,000) = 15,000
        PartyLedgerSummaryResponse summary = partyLedgerService.getPartyLedgerSummary(1L);

        assertNotNull(summary);
        assertEquals(0, new BigDecimal("50000.00").compareTo(summary.getTotalCommissionEarned()));
        assertEquals(0, new BigDecimal("35000.00").compareTo(summary.getTotalCommissionPaid()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(summary.getOutstandingCommission()));
    }

    @Test
    @DisplayName("Requirement 18: Party Ledger calculation (chronological entries and running balance)")
    void testPartyLedgerRunningBalanceAndOrdering() {
        // Event 1: 2026-08-01 -> Payable of 50,000 (Credit: 50,000, Debit: 0, Balance: 50,000)
        PartyPayableEntity payable = new PartyPayableEntity();
        payable.setId(100L);
        payable.setPayableAmount(new BigDecimal("50000.00"));
        payable.setTransactionDate(LocalDate.of(2026, 8, 1));
        payable.setStatus(StatusEnum.A);
        payable.setService(service);
        payable.setCalculationBasis("MONTHLY_FIXED");
        payable.setSourceReference("MF-2026-08");

        // Event 2: 2026-08-10 -> Payment 1 of 20,000 (Credit: 0, Debit: 20,000, Balance: 30,000)
        CommissionPaymentEntity payment1 = new CommissionPaymentEntity();
        payment1.setCommissionPaymentId(1L);
        payment1.setPaymentNo("PAY-001");
        payment1.setPaymentAmount(new BigDecimal("20000.00"));
        payment1.setPaymentDate(LocalDate.of(2026, 8, 10));
        payment1.setStatus("ACTIVE");

        // Event 3: 2026-08-20 -> Payment 2 of 15,000 (Credit: 0, Debit: 15,000, Balance: 15,000)
        CommissionPaymentEntity payment2 = new CommissionPaymentEntity();
        payment2.setCommissionPaymentId(2L);
        payment2.setPaymentNo("PAY-002");
        payment2.setPaymentAmount(new BigDecimal("15000.00"));
        payment2.setPaymentDate(LocalDate.of(2026, 8, 20));
        payment2.setStatus("ACTIVE");

        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("50000.00"));
        when(partyPayableRepository.findByParty_IdAndStatus(1L, StatusEnum.A)).thenReturn(List.of(payable));
        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE"))
                .thenReturn(List.of(payment1, payment2));
        when(partyPaymentAdjustmentRepository.findByCommissionPayment_Party_Id(1L))
                .thenReturn(java.util.Collections.emptyList());

        PartyLedgerFilter filter = new PartyLedgerFilter();
        filter.setPage(0);
        filter.setSize(10);

        PartyLedgerResponse ledger = partyLedgerService.getPartyLedger(1L, filter);

        assertNotNull(ledger);
        assertEquals(0, new BigDecimal("50000.00").compareTo(ledger.getTotalCommissionEarned()));
        assertEquals(0, new BigDecimal("35000.00").compareTo(ledger.getTotalCommissionPaid()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(ledger.getOutstandingCommission()));

        List<PartyLedgerEntryResponse> entries = ledger.getEntries();
        assertEquals(3, entries.size());

        // First entry: Payable
        PartyLedgerEntryResponse entry1 = entries.get(0);
        assertEquals(LocalDate.of(2026, 8, 1), entry1.getLedgerDate());
        assertEquals("PARTY_PAYABLE", entry1.getTransactionType());
        assertEquals(0, new BigDecimal("50000.00").compareTo(entry1.getCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(entry1.getDebit()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(entry1.getBalance()));

        // Second entry: Payment 1
        PartyLedgerEntryResponse entry2 = entries.get(1);
        assertEquals(LocalDate.of(2026, 8, 10), entry2.getLedgerDate());
        assertEquals("COMMISSION_PAYMENT", entry2.getTransactionType());
        assertEquals(0, BigDecimal.ZERO.compareTo(entry2.getCredit()));
        assertEquals(0, new BigDecimal("20000.00").compareTo(entry2.getDebit()));
        assertEquals(0, new BigDecimal("30000.00").compareTo(entry2.getBalance()));

        // Third entry: Payment 2
        PartyLedgerEntryResponse entry3 = entries.get(2);
        assertEquals(LocalDate.of(2026, 8, 20), entry3.getLedgerDate());
        assertEquals("COMMISSION_PAYMENT", entry3.getTransactionType());
        assertEquals(0, BigDecimal.ZERO.compareTo(entry3.getCredit()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(entry3.getDebit()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(entry3.getBalance()));
    }
}
