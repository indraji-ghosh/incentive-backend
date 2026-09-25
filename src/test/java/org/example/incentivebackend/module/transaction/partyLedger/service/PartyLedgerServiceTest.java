package org.example.incentivebackend.module.transaction.partyLedger.service;

import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionCalculationService;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartyLedgerServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private CommissionPaymentRepository commissionPaymentRepository;

    @Mock
    private CommissionCalculationService commissionCalculationService;

    @Mock
    private org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository partyPayableRepository;

    @Mock
    private org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository partyPaymentAdjustmentRepository;

    @InjectMocks
    private PartyLedgerService partyLedgerService;

    private PartyEntity party;
    private PartyLedgerFilter defaultFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(partyPayableRepository.findByParty_IdAndStatus(any(), any())).thenReturn(Collections.emptyList());
        when(partyPaymentAdjustmentRepository.findByAdvancePayment_Party_Id(any())).thenReturn(Collections.emptyList());
        party = new PartyEntity();
        party.setId(1L);
        party.setPartyName("Test Party");
        party.setCreatedAt(LocalDateTime.of(2026, 8, 1, 10, 0));

        defaultFilter = new PartyLedgerFilter();
        defaultFilter.setPage(0);
        defaultFilter.setSize(10);
    }

    @Test
    void testPartyLedgerWithNoCommission() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(BigDecimal.ZERO);
        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Collections.emptyList());

        PartyLedgerResponse response = partyLedgerService.getPartyLedger(1L, defaultFilter);

        assertEquals(0, response.getTotalCommissionEarned().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getTotalCommissionPaid().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getOutstandingCommission().compareTo(BigDecimal.ZERO));
        assertTrue(response.getEntries().isEmpty());
    }

    @Test
    void testPartyWithCommissionEarnedAndNoPayments() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("100000.00"));
        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Collections.emptyList());

        PartyLedgerResponse response = partyLedgerService.getPartyLedger(1L, defaultFilter);

        assertEquals(0, response.getTotalCommissionEarned().compareTo(new BigDecimal("100000.00")));
        assertEquals(0, response.getTotalCommissionPaid().compareTo(BigDecimal.ZERO));
        assertEquals(0, response.getOutstandingCommission().compareTo(new BigDecimal("100000.00")));
        assertEquals(1, response.getEntries().size());
    }

    @Test
    void testPartyWithOneCommissionPayment() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("100000.00"));

        CommissionPaymentEntity cp1 = new CommissionPaymentEntity();
        cp1.setPaymentDate(LocalDate.of(2026, 8, 10));
        cp1.setPaymentAmount(new BigDecimal("40000.00"));
        cp1.setPaymentNo("CP-001");
        cp1.setParty(party);

        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Collections.singletonList(cp1));

        PartyLedgerResponse response = partyLedgerService.getPartyLedger(1L, defaultFilter);

        assertEquals(0, response.getOutstandingCommission().compareTo(new BigDecimal("60000.00")));
        assertEquals(2, response.getEntries().size());
        
        // Final balance should be 60000
        assertEquals(0, response.getEntries().get(1).getBalance().compareTo(new BigDecimal("60000.00")));
    }

    @Test
    void testRunningBalanceAndOrdering() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));

        CommissionPaymentEntity cp1 = new CommissionPaymentEntity();
        cp1.setPaymentDate(LocalDate.of(2026, 8, 10));
        cp1.setPaymentAmount(new BigDecimal("40000.00"));
        cp1.setPaymentNo("CP-001");

        CommissionPaymentEntity cp2 = new CommissionPaymentEntity();
        cp2.setPaymentDate(LocalDate.of(2026, 8, 20));
        cp2.setPaymentAmount(new BigDecimal("20000.00"));
        cp2.setPaymentNo("CP-002");

        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Arrays.asList(cp2, cp1)); // deliberately unordered

        PartyLedgerResponse response = partyLedgerService.getPartyLedger(1L, defaultFilter);

        assertEquals(0, response.getTotalCommissionEarned().compareTo(new BigDecimal("150000.00")));
        assertEquals(0, response.getTotalCommissionPaid().compareTo(new BigDecimal("60000.00")));
        assertEquals(0, response.getOutstandingCommission().compareTo(new BigDecimal("90000.00")));

        assertEquals(3, response.getEntries().size());

        // Entry 0: EARNED (150000)
        assertEquals(0, response.getEntries().get(0).getBalance().compareTo(new BigDecimal("150000.00")));
        
        // Entry 1: CP-001 on Aug 10 (40000 debit => balance 110000)
        assertEquals(0, response.getEntries().get(1).getBalance().compareTo(new BigDecimal("110000.00")));
        assertEquals("CP-001", response.getEntries().get(1).getReferenceNo());

        // Entry 2: CP-002 on Aug 20 (20000 debit => balance 90000)
        assertEquals(0, response.getEntries().get(2).getBalance().compareTo(new BigDecimal("90000.00")));
        assertEquals("CP-002", response.getEntries().get(2).getReferenceNo());
    }

    @Test
    void testDateFiltering() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));

        CommissionPaymentEntity cp1 = new CommissionPaymentEntity();
        cp1.setPaymentDate(LocalDate.of(2026, 8, 10));
        cp1.setPaymentAmount(new BigDecimal("40000.00"));
        cp1.setPaymentNo("CP-001");

        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Collections.singletonList(cp1));

        PartyLedgerFilter filter = new PartyLedgerFilter();
        filter.setFromDate(LocalDate.of(2026, 8, 5));
        filter.setToDate(LocalDate.of(2026, 8, 15));

        PartyLedgerResponse response = partyLedgerService.getPartyLedger(1L, filter);

        // Earned entry is dated 2026-08-01 (from party.createdAt), so it gets filtered out by fromDate=2026-08-05
        // Only CP-001 (Aug 10) remains in filtered view.
        // But running balance should still reflect the 150000 from before!
        assertEquals(1, response.getEntries().size());
        assertEquals("CP-001", response.getEntries().get(0).getReferenceNo());
        assertEquals(0, response.getEntries().get(0).getBalance().compareTo(new BigDecimal("110000.00")));
    }

    @Test
    void testPartyNotFound() {
        when(partyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> partyLedgerService.getPartyLedger(99L, defaultFilter));
    }

    @Test
    void testSummaryEndpoint() {
        when(partyRepository.findById(1L)).thenReturn(Optional.of(party));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("200000.00"));
        
        CommissionPaymentEntity cp1 = new CommissionPaymentEntity();
        cp1.setPaymentAmount(new BigDecimal("50000.00"));
        
        when(commissionPaymentRepository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(1L, "ACTIVE")).thenReturn(Collections.singletonList(cp1));
        when(commissionPaymentRepository.sumActivePayablePaymentAmountByPartyId(1L))
                .thenReturn(new BigDecimal("50000.00"));
        when(commissionPaymentRepository.sumAdjustedAdvanceAmountByPartyId(1L))
                .thenReturn(BigDecimal.ZERO);

        PartyLedgerSummaryResponse response = partyLedgerService.getPartyLedgerSummary(1L);

        assertEquals(0, response.getTotalCommissionEarned().compareTo(new BigDecimal("200000.00")));
        assertEquals(0, response.getTotalCommissionPaid().compareTo(new BigDecimal("50000.00")));
        assertEquals(0, response.getOutstandingCommission().compareTo(new BigDecimal("150000.00")));
    }
}
