package org.example.incentivebackend.module.transaction.bill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.module.transaction.bill.dto.BillAnnexureRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BillRequest {

    @NotBlank(message = "Bill number is required")
    @Size(max = 50)
    private String billNumber;

    @NotNull(message = "Working month is required")
    private LocalDate workingMonth;

    @NotNull(message = "Client is required")
    private Long clientId;

    private Long partyId;

    @NotNull(message = "Site (Siding) is required")
    private Long siteId;

    @NotNull(message = "Bill amount is required")
    @DecimalMin(
            value = "0.00",
            message = "Bill amount cannot be negative"
    )
    private BigDecimal billAmount;

    @Size(max = 500)
    private String remarks;

    @NotEmpty(message = "At least one annexure row is required")
    private List<@Valid BillAnnexureRequest> annexures;

    @NotEmpty(message = "At least one service is required")
    @NotNull(message = "Service is required")
    private Long serviceId;
}