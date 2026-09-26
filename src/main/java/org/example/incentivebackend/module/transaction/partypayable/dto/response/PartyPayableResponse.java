package org.example.incentivebackend.module.transaction.partypayable.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PartyPayableResponse {

    private Long id;
    private Long partyId;
    private String partyName;
    private Long partyAssignmentId;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private Long serviceId;
    private String serviceName;
    private String sourceType;
    private String sourceId;
    private String sourceReference;
    private String calculationBasis;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal payableAmount;
    private LocalDate transactionDate;
    private StatusEnum status;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String remarks;
    private LocalDateTime createdAt;
}
