package org.example.incentivebackend.module.transaction.commissionpayment.dto;

import lombok.Data;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CommissionPaymentResponseDTO {
    private Long id;
    private Long partyId;
    private String partyName;
    private BigDecimal paymentAmount;
    private LocalDate paymentDate;
    private String paymentReference;
    private String paymentMethod;
    private String remarks;
    private StatusEnum paymentStatus;
    private List<CommissionPaymentAllocationResponseDTO> allocations;
}
