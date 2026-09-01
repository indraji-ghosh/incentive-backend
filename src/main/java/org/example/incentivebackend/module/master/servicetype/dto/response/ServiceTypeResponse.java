package org.example.incentivebackend.module.master.servicetype.dto.response;

import lombok.Data;

@Data
public class ServiceTypeResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
}
