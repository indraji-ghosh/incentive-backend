package org.example.incentivebackend.module.transaction.clientpayment.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClientPaymentResponse {
    private Long clientPaymentId;
    private String paymentNo;
    private Long billId;
    private String billNo;
    private Long clientId;
    private String clientName;
    private LocalDate paymentDate;
    private BigDecimal paymentAmount;
    private String remarks;
    private String createdAt;
    private String updatedAt;
}
