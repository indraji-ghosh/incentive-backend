package org.example.incentivebackend.module.association.clientSite.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteRequest;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;
import org.example.incentivebackend.module.association.clientSite.entity.ClientSiteEntity;
import org.example.incentivebackend.module.association.clientSite.mapper.ClientSiteMapper;
import org.example.incentivebackend.module.association.clientSite.repository.ClientSiteRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientSiteServiceImpl
        implements ClientSiteService {

    private final ClientSiteRepository clientSiteRepository;

    private final ClientRepository clientRepository;

    private final SiteRepository siteRepository;

    private final ClientSiteMapper clientSiteMapper;

    @Override
    public List<ClientSiteResponse> assign(
            ClientSiteRequest request
    ) {

        ClientEntity client =
                clientRepository.findById(request.getClientId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Client not found: "
                                                + request.getClientId()
                                )
                        );

        List<ClientSiteEntity> associations = new ArrayList<>();

        for (Long siteId : request.getSiteIds()) {

            SiteEntity site =
                    siteRepository.findById(siteId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Site not found: " + siteId
                                    )
                            );

            if (clientSiteRepository
                    .existsByClientClientIdAndSiteSiteId(
                            request.getClientId(),
                            siteId
                    )) {

                throw new DuplicateResourceException(
                        "Site " + siteId +
                                " is already assigned to this client"
                );
            }

            ClientSiteEntity entity =
                    new ClientSiteEntity();

            entity.setClient(client);
            entity.setSite(site);

            associations.add(entity);
        }

        List<ClientSiteEntity> saved =
                clientSiteRepository.saveAll(associations);

        return clientSiteMapper.toResponseList(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientSiteResponse> findByClientId(
            Long clientId
    ) {

        if (!clientRepository.existsById(clientId)) {

            throw new ResourceNotFoundException(
                    "Client not found: " + clientId
            );
        }

        return clientSiteMapper.toResponseList(
                clientSiteRepository.findByClientClientId(
                        clientId
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientSiteResponse> findBySiteId(Long siteId) {

        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException(
                    "Site not found: " + siteId
            );
        }

        List<ClientSiteEntity> associations =
                clientSiteRepository.findBySiteSiteId(siteId);

        return clientSiteMapper.toResponseList(
                associations
        );
    }

    @Override
    public void remove(
            Long clientId,
            Long siteId
    ) {

        ClientSiteEntity entity =
                clientSiteRepository
                        .findByClientClientIdAndSiteSiteId(
                                clientId,
                                siteId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Client Site association not found"
                                )
                        );

        clientSiteRepository.delete(entity);
    }
}