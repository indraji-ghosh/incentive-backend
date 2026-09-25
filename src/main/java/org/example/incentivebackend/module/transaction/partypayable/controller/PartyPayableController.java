package org.example.incentivebackend.module.transaction.partypayable.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.partypayable.dto.request.PartyPayableFilter;
import org.example.incentivebackend.module.transaction.partypayable.dto.response.PartyPayableResponse;
import org.example.incentivebackend.module.transaction.partypayable.service.PartyPayableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction/party-payables")
@RequiredArgsConstructor
public class PartyPayableController {

    private final PartyPayableService partyPayableService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartyPayableResponse>>> findAll(
            @ModelAttribute PartyPayableFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<PartyPayableResponse> response = partyPayableService.findAll(filter, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Party Payables", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyPayableResponse>> findById(@PathVariable Long id) {
        PartyPayableResponse response = partyPayableService.findById(id);
        return ResponseBuilder.fetched("Party Payable", response);
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<ApiResponse<List<PartyPayableResponse>>> findByParty(@PathVariable Long partyId) {
        List<PartyPayableResponse> response = partyPayableService.findByParty(partyId);
        return ResponseBuilder.list("Party Payables", response);
    }

    @PostMapping("/generate/bill/{billId}")
    public ResponseEntity<ApiResponse<List<PartyPayableResponse>>> generateForBill(@PathVariable Long billId) {
        List<PartyPayableResponse> response = partyPayableService.generateAndGetResponsesForBillId(billId);
        return ResponseBuilder.created("Party Payables generated for bill", response);
    }
}
