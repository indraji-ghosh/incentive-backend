package org.example.incentivebackend.module.transaction.partyLedger.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PartyLedgerFilter {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    
    private String transactionType; // Optional filter (e.g. COMMISSION_EARNED, COMMISSION_PAYMENT)
    
    private int page = 0;
    private int size = 10;
    private String sort; // e.g. "ledgerDate,desc"
}
