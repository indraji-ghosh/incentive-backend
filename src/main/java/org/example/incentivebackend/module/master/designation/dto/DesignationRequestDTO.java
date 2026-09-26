package org.example.incentivebackend.module.master.designation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DesignationRequestDTO {

    @NotBlank(message = "Designation code is required")
    private String designationCode;

    @NotBlank(message = "Designation name is required")
    private String designationName;

    private String level;

    private String description;

    private Boolean isActive;
}
