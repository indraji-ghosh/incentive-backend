package org.example.incentivebackend.module.transaction.commissionpayment.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CommissionPaymentResponse {
    private Long commissionPaymentId;
    private String paymentNo;
    private Long partyId;
    private String partyName;
    private LocalDate paymentDate;
    private BigDecimal paymentAmount;
    private String remarks;
    private String status;
    private LocalDateTime createdAt;
    private Long createdBy;
    private String paymentType;
    private BigDecimal adjustedAmount;
    private Long submittedBy;
    private String submittedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private Long rejectedBy;
    private String rejectedByName;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO approval;
    private java.util.List<PaymentAllocationResponse> allocations;

    @Data
    public static class PaymentAllocationResponse {
        private Long accruedPayableId;
        private String payableType;
        private String reference;
        private BigDecimal originalAmount;
        private BigDecimal previouslyPaid;
        private BigDecimal allocatedAmount;
        private BigDecimal remainingAmount;
    }
}
