package org.example.incentivebackend.module.association.partyassignment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PartyServiceConfigurationRequest {

    private Long id;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Payment Type ID is required")
    private Long paymentTypeId;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Rate must be greater than 0")
    private BigDecimal rate;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private StatusEnum status = StatusEnum.A;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
