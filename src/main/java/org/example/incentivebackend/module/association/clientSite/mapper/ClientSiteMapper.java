package org.example.incentivebackend.module.association.clientSite.mapper;

import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;
import org.example.incentivebackend.module.association.clientSite.entity.ClientSiteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface ClientSiteMapper {

    @Mapping(
            target = "clientId",
            source = "client.clientId"
    )
    @Mapping(
            target = "clientName",
            source = "client.clientName"
    )
    @Mapping(
            target = "siteId",
            source = "site.siteId"
    )
    @Mapping(
            target = "siteName",
            source = "site.siteName"
    )
    @Mapping(
            target = "siteShortCode",
            source = "site.siteShortCode"
    )
    ClientSiteResponse toResponse(
            ClientSiteEntity entity
    );

    List<ClientSiteResponse> toResponseList(
            List<ClientSiteEntity> entities
    );
}