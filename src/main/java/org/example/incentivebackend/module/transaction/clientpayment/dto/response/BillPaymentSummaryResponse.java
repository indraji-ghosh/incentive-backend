package org.example.incentivebackend.module.transaction.clientpayment.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BillPaymentSummaryResponse {
    private Long billId;
    private String billNo;
    private BigDecimal billAmount;
    private BigDecimal totalPaid;
    private BigDecimal outstandingAmount;
    private String paymentStatus;
}
