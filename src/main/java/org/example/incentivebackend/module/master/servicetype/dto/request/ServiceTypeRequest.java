package org.example.incentivebackend.module.master.servicetype.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ServiceTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private Boolean isActive;
}
