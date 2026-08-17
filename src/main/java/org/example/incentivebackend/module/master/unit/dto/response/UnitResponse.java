package org.example.incentivebackend.module.master.unit.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnitResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
}
