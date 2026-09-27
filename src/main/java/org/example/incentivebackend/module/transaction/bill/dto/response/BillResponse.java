package org.example.incentivebackend.module.transaction.bill.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillAnnexureResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class BillResponse {

    private Long billId;

    private String billNumber;

    private LocalDate workingMonth;

    private Long clientId;

    private String clientName;

    private Long partyId;

    private String partyName;

    private Long siteId;

    private String siteName;

    private String siteShortCode;

    private BigDecimal billAmount;

    private String paymentStatus;

    private BigDecimal paidAmount;

    private BigDecimal outstandingAmount;

    private String remarks;

    private Integer totalRr;

    private Integer totalWagons;

    private List<BillAnnexureResponse> annexures;

    private Long serviceId;
    
    private String serviceName;
}