package org.example.incentivebackend.module.master.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.enums.StatusEnum;

@Getter
@Setter
public class SiteRequest {

    @NotBlank(message = "Site name is required")
    @Size(
            max = 100,
            message = "Site name cannot exceed 100 characters"
    )
    private String siteName;

    @NotBlank(message = "Site short code is required")
    @Size(
            max = 20,
            message = "Site short code cannot exceed 20 characters"
    )
    private String siteShortCode;

    @Size(
            max = 100,
            message = "State cannot exceed 100 characters"
    )
    private String state;


    private StatusEnum siteStatus;
}