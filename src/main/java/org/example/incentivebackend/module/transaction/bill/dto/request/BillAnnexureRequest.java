package org.example.incentivebackend.module.transaction.bill.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BillAnnexureRequest {

    @NotBlank(message = "RR number is required")
    @Size(max = 100)
    private String rrNo;

    @NotNull(message = "RR date is required")
    private LocalDate rrDate;

    @Size(max = 100)
    private String challan;

    private LocalDate loadDate;

    @NotBlank(message = "Siding is required")
    @Size(max = 150)
    private String siding;

    @Size(max = 150)
    private String destination;

    @NotNull(message = "Wagons is required")
    @Min(
            value = 0,
            message = "Wagons cannot be negative"
    )
    private Integer wagons;

    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private java.math.BigDecimal weight;
}