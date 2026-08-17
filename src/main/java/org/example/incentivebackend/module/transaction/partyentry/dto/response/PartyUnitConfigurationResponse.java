package org.example.incentivebackend.module.transaction.partyentry.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PartyUnitConfigurationResponse {
    private Long id;
    private Long unitId;
    private String unitName;
    private BigDecimal rate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String notes;
}
