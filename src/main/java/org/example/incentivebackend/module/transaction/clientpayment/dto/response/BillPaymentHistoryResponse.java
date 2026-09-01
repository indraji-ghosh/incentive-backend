package org.example.incentivebackend.module.transaction.clientpayment.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BillPaymentHistoryResponse {
    private Long billId;
    private String billNo;
    private Long clientId;
    private String clientName;
    private BigDecimal billAmount;
    private BigDecimal totalPaid;
    private BigDecimal outstandingAmount;
    private String paymentStatus;
    
    private List<ClientPaymentResponse> payments;
}
