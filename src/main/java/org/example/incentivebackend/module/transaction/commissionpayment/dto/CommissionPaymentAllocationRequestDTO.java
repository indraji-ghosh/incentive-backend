package org.example.incentivebackend.module.transaction.commissionpayment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.example.incentivebackend.module.transaction.commissionpayment.enums.CommissionReferenceType;

import java.math.BigDecimal;

@Data
public class CommissionPaymentAllocationRequestDTO {

    @NotNull(message = "Reference ID is required")
    private Long referenceId;

    @NotNull(message = "Reference type is required")
    private CommissionReferenceType referenceType;

    @NotNull(message = "Allocated amount is required")
    @Positive(message = "Allocated amount must be positive")
    private BigDecimal allocatedAmount;
}
