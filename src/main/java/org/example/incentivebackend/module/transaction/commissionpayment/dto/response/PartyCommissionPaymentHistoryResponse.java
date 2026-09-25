package org.example.incentivebackend.module.transaction.commissionpayment.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PartyCommissionPaymentHistoryResponse {
    private Long partyId;
    private String partyName;
    private BigDecimal totalCommissionEarned;
    private BigDecimal totalCommissionPaid;
    private BigDecimal outstandingCommission;
    private String paymentStatus;
    private List<CommissionPaymentResponse> payments;
    private BigDecimal totalAdvance;
    private BigDecimal totalAdvanceAdjusted;
    private BigDecimal availableAdvance;
}
