package org.example.incentivebackend.module.transaction.partyLedger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLedgerResponse {
    private Long partyId;
    private String partyName;
    private BigDecimal totalCommissionEarned;
    private BigDecimal totalCommissionPaid;
    private BigDecimal outstandingCommission;
    private List<PartyLedgerEntryResponse> entries;
}
