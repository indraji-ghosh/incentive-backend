package org.example.incentivebackend.module.master.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class SectorRequest {

    @NotBlank(message = "Sector name is required")
    @Size(
            max = 150,
            message = "Sector name cannot exceed 150 characters"
    )
    private String sectorName;

    @NotBlank(message = "Sector short code is required")
    @Size(
            max = 20,
            message = "Sector short code cannot exceed 20 characters"
    )
    private String sectorShortCode;

    private StatusEnum sectorStatus;
}