package org.example.incentivebackend.module.transaction.bill.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BillAnnexureResponse {

    private Long billAnnexureId;

    private String rrNo;

    private LocalDate rrDate;

    private String challan;

    private LocalDate loadDate;

    private String siding;

    private String destination;

    private Integer wagons;
}