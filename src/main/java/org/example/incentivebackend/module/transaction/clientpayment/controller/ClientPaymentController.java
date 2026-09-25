package org.example.incentivebackend.module.transaction.clientpayment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.ClientPaymentResponse;
import org.example.incentivebackend.module.transaction.clientpayment.service.ClientPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/transaction/client-payments", "/api/transaction/client-payments"})
@RequiredArgsConstructor
public class ClientPaymentController {

    private final ClientPaymentService clientPaymentService;
    private static final String RESOURCE = "Client Payment";

    @PostMapping
    public ResponseEntity<ApiResponse<ClientPaymentResponse>> createPayment(
            @Valid @RequestBody ClientPaymentRequest request
    ) {
        ClientPaymentResponse response = clientPaymentService.createPayment(request);
        return ResponseBuilder.created(RESOURCE, response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientPaymentResponse>> updatePayment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ClientPaymentRequest request
    ) {
        ClientPaymentResponse response = clientPaymentService.updatePayment(id, request);
        return ResponseBuilder.updated(RESOURCE, response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientPaymentResponse>> getPaymentById(
            @PathVariable("id") Long id
    ) {
        ClientPaymentResponse response = clientPaymentService.getPaymentById(id);
        return ResponseBuilder.fetched(RESOURCE, response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClientPaymentResponse>>> getAllPayments(
            Pageable pageable
    ) {
        Page<ClientPaymentResponse> response = clientPaymentService.getAllPayments(pageable);
        return ResponseBuilder.list(RESOURCE, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePayment(
            @PathVariable("id") Long id
    ) {
        clientPaymentService.deletePayment(id);
        return ResponseBuilder.deleted(RESOURCE);
    }

    @GetMapping("/bill/{billId}")
    public ResponseEntity<ApiResponse<BillPaymentHistoryResponse>> getBillPaymentHistory(
            @PathVariable("billId") Long billId
    ) {
        BillPaymentHistoryResponse response = clientPaymentService.getBillPaymentHistory(billId);
        return ResponseBuilder.fetched("Bill Payment History", response);
    }

    @GetMapping("/bill/{billId}/summary")
    public ResponseEntity<ApiResponse<BillPaymentSummaryResponse>> getBillPaymentSummary(
            @PathVariable("billId") Long billId
    ) {
        BillPaymentSummaryResponse response = clientPaymentService.getBillPaymentSummary(billId);
        return ResponseBuilder.fetched("Bill Payment Summary", response);
    }
}
