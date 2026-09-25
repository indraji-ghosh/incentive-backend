package org.example.incentivebackend.module.transaction.partypayable.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDate;

@Getter
@Setter
public class PartyPayableFilter {

    private Long partyId;
    private Long clientId;
    private Long siteId;
    private Long serviceId;
    private String sourceType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private StatusEnum status;
}
