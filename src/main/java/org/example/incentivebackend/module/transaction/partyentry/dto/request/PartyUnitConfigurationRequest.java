package org.example.incentivebackend.module.transaction.partyentry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PartyUnitConfigurationRequest {

    private Long id; // Optional, used for updates

    @NotNull(message = "Unit is required")
    private Long unitId;

    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be positive")
    private BigDecimal rate;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
