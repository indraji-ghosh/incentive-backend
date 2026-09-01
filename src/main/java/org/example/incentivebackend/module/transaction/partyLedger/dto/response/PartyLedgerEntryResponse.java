package org.example.incentivebackend.module.transaction.partyLedger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyLedgerEntryResponse {
    private LocalDate ledgerDate;
    private String transactionType; // COMMISSION_EARNED, COMMISSION_PAYMENT
    private String referenceNo;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal amount;
    private BigDecimal balance;
}
