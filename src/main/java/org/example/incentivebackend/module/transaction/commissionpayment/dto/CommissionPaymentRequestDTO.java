package org.example.incentivebackend.module.transaction.commissionpayment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommissionPaymentRequestDTO {

    @NotNull(message = "Party ID is required")
    private Long partyId;

    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private String paymentReference;

    private String paymentMethod;

    private String remarks;
    
    @NotNull(message = "Payment status is required")
    private StatusEnum paymentStatus;

    @NotEmpty(message = "Allocations cannot be empty")
    @Valid
    private List<CommissionPaymentAllocationRequestDTO> allocations;
}
