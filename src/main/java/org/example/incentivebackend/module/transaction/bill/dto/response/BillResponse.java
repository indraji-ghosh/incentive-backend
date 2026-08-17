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

    private BigDecimal billAmount;

    private String remarks;

    private Integer totalRr;

    private Integer totalWagons;

    private List<BillAnnexureResponse> annexures;
}