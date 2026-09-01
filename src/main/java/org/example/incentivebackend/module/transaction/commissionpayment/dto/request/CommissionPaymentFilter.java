package org.example.incentivebackend.module.transaction.commissionpayment.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CommissionPaymentFilter {
    private Long partyId;
    private String partyName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String paymentStatus;
    
    private int page = 0;
    private int size = 10;
}
