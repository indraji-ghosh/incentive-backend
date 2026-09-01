package org.example.incentivebackend.module.transaction.rakeEntry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RakeAnnexureRequest {

    private Long rakeAnnexureId;

    @NotBlank(message = "RR No is required")
    private String rrNo;

    @NotNull(message = "RR Date is required")
    private LocalDate rrDate;

    private String challan;

    private LocalDate loadDate;

    private String siding;

    private String destination;

    private Integer wagons;

    private BigDecimal weight;

    private BigDecimal quantity;

    private BigDecimal annexureAmount;

    private String notes;

    private StatusEnum status;
}
