package org.example.incentivebackend.module.transaction.commissionpayment.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommissionPaymentResponse {
    private Long commissionPaymentId;
    private String paymentNo;
    private Long partyId;
    private String partyName;
    private LocalDate paymentDate;
    private BigDecimal paymentAmount;
    private String remarks;
    private String status;
    private LocalDateTime createdAt;
    private Long createdBy;
}
