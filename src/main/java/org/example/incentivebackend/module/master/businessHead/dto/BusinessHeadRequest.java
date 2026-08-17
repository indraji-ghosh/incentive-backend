package org.example.incentivebackend.module.master.businessHead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class BusinessHeadRequest {

    @NotBlank(message = "Business head name is required")
    @Size(max = 100, message = "Business head name cannot exceed 100 characters")
    private String headName;

    @NotBlank(message = "Business head short code is required")
    @Size(max = 50, message = "Business head short code cannot exceed 50 characters")
    private String headShortCode;

    private StatusEnum headStatus;
}