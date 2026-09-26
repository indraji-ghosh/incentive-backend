package org.example.incentivebackend.module.transaction.commissionpayment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommissionPaymentRequest {

    @NotNull(message = "Party ID is required")
    private Long partyId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal paymentAmount;

    private String remarks;

    private List<PaymentAllocationRequest> allocations;

    @Data
    public static class PaymentAllocationRequest {
        @NotNull(message = "Payable ID is required")
        private Long payableId;

        @NotNull(message = "Allocation amount is required")
        @DecimalMin(value = "0.01", message = "Allocation amount must be greater than zero")
        private BigDecimal amount;
    }
}
