package org.example.incentivebackend.module.association.clientSite.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientSiteResponse {

    private Long clientSiteId;

    private Long clientId;

    private String clientName;

    private Long siteId;

    private String siteName;

    private String siteShortCode;
}