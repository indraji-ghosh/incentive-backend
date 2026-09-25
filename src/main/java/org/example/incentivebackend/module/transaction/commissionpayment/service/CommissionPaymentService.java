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
}
