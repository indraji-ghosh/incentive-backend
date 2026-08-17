package org.example.incentivebackend.module.association.clientSite.service;

import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteRequest;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;

import java.util.List;

public interface ClientSiteService {

    List<ClientSiteResponse> assign(
            ClientSiteRequest request
    );

    List<ClientSiteResponse> findByClientId(
            Long clientId
    );

    List<ClientSiteResponse> findBySiteId(
            Long siteId
    );

    void remove(
            Long clientId,
            Long siteId
    );
}