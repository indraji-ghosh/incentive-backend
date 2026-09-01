package org.example.incentivebackend.module.transaction.rakeEntry.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RakeAnnexureResponse {

    private Long rakeAnnexureId;
    private String rrNo;
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
