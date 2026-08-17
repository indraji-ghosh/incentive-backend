package org.example.incentivebackend.module.association.clientSite.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClientSiteRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotEmpty(message = "At least one site is required")
    private List<Long> siteIds;
}