package org.example.incentivebackend.module.transaction.commissionpayment.service;

import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentFilter;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.PartyCommissionPaymentHistoryResponse;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;

public interface CommissionPaymentService {
    CommissionPaymentResponse create(CommissionPaymentRequest request);
    Page<CommissionPaymentSummaryResponse> getSummary(CommissionPaymentFilter filter);
    CommissionPaymentResponse getById(Long id);
    PartyCommissionPaymentHistoryResponse getPartyHistory(Long partyId);
    CommissionPaymentResponse update(Long id, CommissionPaymentRequest request);
    void delete(Long id);
    
    // New Advance Payment Methods
    CommissionPaymentResponse createAdvancePayment(CommissionPaymentRequest request);
    CommissionPaymentResponse adjustAdvance(Long partyId, Long payableId, BigDecimal adjustmentAmount);
    BigDecimal getAvailableAdvanceBalance(Long partyId);

    // Approval Operations
    CommissionPaymentResponse submit(Long id, String remarks);
    CommissionPaymentResponse approve(Long id, String remarks);
    CommissionPaymentResponse reject(Long id, String reason);
    CommissionPaymentResponse pay(Long id);
    CommissionPaymentResponse cancel(Long id, String reason);
    org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO getApproval(Long id);
    Page<CommissionPaymentResponse> getVouchers(int page, int size, String status, Long partyId, String search);
}
