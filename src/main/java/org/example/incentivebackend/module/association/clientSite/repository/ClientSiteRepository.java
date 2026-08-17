package org.example.incentivebackend.module.association.clientSite.repository;

import org.example.incentivebackend.module.association.clientSite.entity.ClientSiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientSiteRepository
        extends JpaRepository<ClientSiteEntity, Long> {

    boolean existsByClientClientIdAndSiteSiteId(
            Long clientId,
            Long siteId
    );

    List<ClientSiteEntity> findByClientClientId(
            Long clientId
    );

    List<ClientSiteEntity> findBySiteSiteId(
            Long siteId
    );
    Optional<ClientSiteEntity> findByClientClientIdAndSiteSiteId(
            Long clientId,
            Long siteId
    );
}