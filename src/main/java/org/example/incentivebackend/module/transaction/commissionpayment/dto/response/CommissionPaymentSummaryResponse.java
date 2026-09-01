package org.example.incentivebackend.module.transaction.commissionpayment.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommissionPaymentSummaryResponse {
    private Long partyId;
    private String partyName;
    private BigDecimal totalCommissionEarned;
    private BigDecimal totalCommissionPaid;
    private BigDecimal outstandingCommission;
    private String paymentStatus;
}
