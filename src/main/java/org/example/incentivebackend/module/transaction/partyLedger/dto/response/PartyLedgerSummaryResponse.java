package org.example.incentivebackend.module.transaction.partyLedger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLedgerSummaryResponse {
    private Long partyId;
    private String partyName;
    private BigDecimal totalCommissionEarned;
    private BigDecimal totalCommissionPaid;
    private BigDecimal outstandingCommission;
}
