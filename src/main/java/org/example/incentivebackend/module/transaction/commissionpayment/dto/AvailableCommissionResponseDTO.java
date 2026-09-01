package org.example.incentivebackend.module.transaction.commissionpayment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.incentivebackend.module.transaction.commissionpayment.enums.CommissionReferenceType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableCommissionResponseDTO {
    private Long partyId;
    private Long referenceId;
    private CommissionReferenceType referenceType;
    private BigDecimal totalAmount;
    private BigDecimal alreadyAllocated;
    private BigDecimal availableAmount;
}
