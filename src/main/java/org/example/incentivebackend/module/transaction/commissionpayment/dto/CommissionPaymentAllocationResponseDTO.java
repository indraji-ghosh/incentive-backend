package org.example.incentivebackend.module.transaction.commissionpayment.dto;

import lombok.Data;
import org.example.incentivebackend.module.transaction.commissionpayment.enums.CommissionReferenceType;

import java.math.BigDecimal;

@Data
public class CommissionPaymentAllocationResponseDTO {
    private Long id;
    private Long referenceId;
    private CommissionReferenceType referenceType;
    private BigDecimal allocatedAmount;
}
