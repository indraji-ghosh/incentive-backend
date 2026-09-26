package org.example.incentivebackend.module.transaction.partyLedger.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.partyLedger.dto.request.PartyLedgerFilter;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerResponse;
import org.example.incentivebackend.module.transaction.partyLedger.dto.response.PartyLedgerSummaryResponse;
import org.example.incentivebackend.module.transaction.partyLedger.service.PartyLedgerService;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction/ledger")
@RequiredArgsConstructor
public class PartyLedgerController {

    private final PartyLedgerService partyLedgerService;

    @GetMapping("/party/{partyId}")
    public ResponseEntity<ApiResponse<PartyLedgerResponse>> getPartyLedger(
            @PathVariable Long partyId,
            @ModelAttribute PartyLedgerFilter filter
    ) {
        PartyLedgerResponse response = partyLedgerService.getPartyLedger(partyId, filter);
        return ResponseBuilder.fetched("Party Ledger", response);
    }

    @GetMapping("/party/{partyId}/summary")
    public ResponseEntity<ApiResponse<PartyLedgerSummaryResponse>> getPartyLedgerSummary(
            @PathVariable Long partyId
    ) {
        PartyLedgerSummaryResponse response = partyLedgerService.getPartyLedgerSummary(partyId);
        return ResponseBuilder.fetched("Party Ledger Summary", response);
    }
}
