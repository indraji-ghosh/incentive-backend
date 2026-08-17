package org.example.incentivebackend.module.master.sector.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.incentivebackend.common.enums.StatusEnum;

import java.time.LocalDateTime;

@Getter
@Builder
public class SectorResponse {

    private Long sectorId;

    private String sectorName;

    private String sectorShortCode;

    private StatusEnum sectorStatus;

    private LocalDateTime createdAt;

    private Long createdBy;

    private LocalDateTime modAt;

    private Long modBy;

    private String appStatus;

    private Long appBy;

    private LocalDateTime appAt;
}