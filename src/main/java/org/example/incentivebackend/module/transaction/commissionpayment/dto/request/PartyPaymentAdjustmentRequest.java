package org.example.incentivebackend.module.transaction.commissionpayment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartyPaymentAdjustmentRequest {
    @NotNull(message = "Party ID is required")
    private Long partyId;

    @NotNull(message = "Payable ID is required")
    private Long payableId;

    @NotNull(message = "Adjustment amount is required")
    @Positive(message = "Adjustment amount must be positive")
    private BigDecimal adjustmentAmount;
}
