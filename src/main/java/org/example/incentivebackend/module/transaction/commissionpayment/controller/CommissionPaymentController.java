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

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> submit(
            @PathVariable Long id,
            @RequestBody(required = false) org.example.incentivebackend.module.approval.dto.ApprovalActionRequestDTO request
    ) {
        String remarks = request != null ? request.getRemarks() : null;
        CommissionPaymentResponse response = commissionPaymentService.submit(id, remarks);
        return ResponseBuilder.updated("Payment Submitted for Approval", response);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> approve(
            @PathVariable Long id,
            @RequestBody(required = false) org.example.incentivebackend.module.approval.dto.ApprovalActionRequestDTO request
    ) {
        String remarks = request != null ? request.getRemarks() : null;
        CommissionPaymentResponse response = commissionPaymentService.approve(id, remarks);
        return ResponseBuilder.updated("Payment Approved", response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody org.example.incentivebackend.module.approval.dto.ApprovalRejectRequestDTO request
    ) {
        CommissionPaymentResponse response = commissionPaymentService.reject(id, request.getReason());
        return ResponseBuilder.updated("Payment Rejected", response);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> pay(
            @PathVariable Long id
    ) {
        CommissionPaymentResponse response = commissionPaymentService.pay(id);
        return ResponseBuilder.updated("Payment Processed as Paid", response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CommissionPaymentResponse>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) org.example.incentivebackend.module.approval.dto.ApprovalRejectRequestDTO request
    ) {
        String reason = request != null ? request.getReason() : "Cancelled by user";
        CommissionPaymentResponse response = commissionPaymentService.cancel(id, reason);
        return ResponseBuilder.updated("Payment Cancelled", response);
    }

    @GetMapping("/{id}/approval")
    public ResponseEntity<ApiResponse<org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO>> getApproval(
            @PathVariable Long id
    ) {
        org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO response = commissionPaymentService.getApproval(id);
        return ResponseBuilder.fetched("Payment Approval Details", response);
    }

    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<Page<CommissionPaymentResponse>>> getVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long partyId,
            @RequestParam(required = false) String search
    ) {
        Page<CommissionPaymentResponse> response = commissionPaymentService.getVouchers(page, size, status, partyId, search);
        return ResponseBuilder.list("Payment Vouchers", response);
    }
}
