package org.example.incentivebackend.module.transaction.clientpayment.service;

import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.ClientPaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientPaymentService {
    ClientPaymentResponse createPayment(ClientPaymentRequest request);
    ClientPaymentResponse updatePayment(Long id, ClientPaymentRequest request);
    ClientPaymentResponse getPaymentById(Long id);
    Page<ClientPaymentResponse> getAllPayments(Pageable pageable);
    void deletePayment(Long id);
    BillPaymentHistoryResponse getBillPaymentHistory(Long billId);
    BillPaymentSummaryResponse getBillPaymentSummary(Long billId);
}
