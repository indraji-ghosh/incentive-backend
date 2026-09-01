package org.example.incentivebackend.module.transaction.partyLedger.controller;

import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.service.PartyLedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PartyLedgerControllerTest {

    @Mock
    private PartyLedgerService partyLedgerService;

    @InjectMocks
    private PartyLedgerController partyLedgerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPartyLedger() {
        PartyLedgerResponse mockResponse = PartyLedgerResponse.builder()
                .partyId(1L)
                .partyName("Test Party")
                .totalCommissionEarned(new BigDecimal("100000.00"))
                .totalCommissionPaid(new BigDecimal("40000.00"))
                .outstandingCommission(new BigDecimal("60000.00"))
                .entries(Collections.emptyList())
                .build();

        when(partyLedgerService.getPartyLedger(eq(1L), any(PartyLedgerFilter.class))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<PartyLedgerResponse>> responseEntity = 
            partyLedgerController.getPartyLedger(1L, new PartyLedgerFilter());

        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals("Party Ledger fetched successfully", responseEntity.getBody().getMessage());
        assertEquals(mockResponse, responseEntity.getBody().getData());
    }

    @Test
    void testGetPartyLedgerSummary() {
        PartyLedgerSummaryResponse mockSummary = PartyLedgerSummaryResponse.builder()
                .partyId(1L)
                .partyName("Test Party")
                .totalCommissionEarned(new BigDecimal("100000.00"))
                .totalCommissionPaid(new BigDecimal("40000.00"))
                .outstandingCommission(new BigDecimal("60000.00"))
                .build();

        when(partyLedgerService.getPartyLedgerSummary(1L)).thenReturn(mockSummary);

        ResponseEntity<ApiResponse<PartyLedgerSummaryResponse>> responseEntity = 
            partyLedgerController.getPartyLedgerSummary(1L);

        assertEquals(200, responseEntity.getStatusCode().value());
        assertNotNull(responseEntity.getBody());
        assertEquals("Party Ledger Summary fetched successfully", responseEntity.getBody().getMessage());
        assertEquals(mockSummary, responseEntity.getBody().getData());
    }
}
