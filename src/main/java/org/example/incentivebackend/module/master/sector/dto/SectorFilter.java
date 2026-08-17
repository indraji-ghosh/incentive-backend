package org.example.incentivebackend.module.master.sector.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class SectorFilter {

    private String search;

    private StatusEnum sectorStatus;

    private int page = 0;

    private int size = 10;

    private String sortBy = "sectorId";

    private String sortDirection = "desc";
}