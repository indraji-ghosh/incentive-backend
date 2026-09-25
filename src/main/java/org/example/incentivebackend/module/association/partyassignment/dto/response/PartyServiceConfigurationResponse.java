package org.example.incentivebackend.module.association.partyassignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyServiceConfigurationResponse {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long paymentTypeId;
    private String paymentTypeCode;
    private String paymentTypeName;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private BigDecimal rate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private StatusEnum status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
