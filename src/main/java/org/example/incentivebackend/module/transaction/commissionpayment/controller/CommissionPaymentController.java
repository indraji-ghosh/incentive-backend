package org.example.incentivebackend.module.transaction.commissionpayment.controller;

import jakarta.validation.Valid;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentFilter;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.PartyPaymentAdjustmentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.PartyCommissionPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transaction/commission-payments")
public class CommissionPaymentController {

    private final CommissionPaymentService commissionPaymentService;

    public CommissionPaymentController(CommissionPaymentService commissionPaymentService) {
        this.commissionPaymentService = commissionPaymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> create(
            @Valid @RequestBody CommissionPaymentRequest request
    ) {
        CommissionPaymentResponse response = commissionPaymentService.create(request);
        return ResponseBuilder.created("Commission Payment", response);
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Page<CommissionPaymentSummaryResponse>>> summary(
            @ModelAttribute CommissionPaymentFilter filter
    ) {
        Page<CommissionPaymentSummaryResponse> response = commissionPaymentService.getSummary(filter);
        return ResponseBuilder.list("Commission Payment Summary", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> getById(
            @PathVariable Long id
    ) {
        CommissionPaymentResponse response = commissionPaymentService.getById(id);
        return ResponseBuilder.fetched("Commission Payment", response);
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<ApiResponse<PartyCommissionPaymentHistoryResponse>> history(
            @PathVariable Long partyId
    ) {
        PartyCommissionPaymentHistoryResponse response = commissionPaymentService.getPartyHistory(partyId);
        return ResponseBuilder.fetched("Commission Payment History", response);
    }

    @PostMapping("/advance")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> createAdvance(
            @Valid @RequestBody CommissionPaymentRequest request
    ) {
        CommissionPaymentResponse response = commissionPaymentService.createAdvancePayment(request);
        return ResponseBuilder.created("Advance Payment", response);
    }

    @PostMapping("/advance/adjust")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> adjustAdvance(
            @Valid @RequestBody PartyPaymentAdjustmentRequest request
    ) {
        CommissionPaymentResponse response = commissionPaymentService.adjustAdvance(
                request.getPartyId(),
                request.getPayableId(),
                request.getAdjustmentAmount()
        );
        return ResponseBuilder.updated("Advance Adjusted", response);
    }

    @GetMapping("/party/{partyId}/advance-balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getAdvanceBalance(
            @PathVariable Long partyId
    ) {
        BigDecimal balance = commissionPaymentService.getAvailableAdvanceBalance(partyId);
        return ResponseBuilder.fetched("Advance Balance", balance);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CommissionPaymentRequest request
    ) {
        CommissionPaymentResponse response = commissionPaymentService.update(id, request);
        return ResponseBuilder.updated("Commission Payment", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable Long id
    ) {
        commissionPaymentService.delete(id);
        return ResponseBuilder.deleted("Commission Payment");
    }
}
