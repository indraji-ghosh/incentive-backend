package org.example.incentivebackend.module.transaction.partyentry.mapper;

import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyUnitConfigurationResponse;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyUnitConfigurationEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PartyEntryMapper {

    @Mapping(target = "clients", ignore = true)
    @Mapping(target = "sites", ignore = true)
    @Mapping(target = "unitConfigurations", ignore = true)
    @Mapping(target = "businessHead", ignore = true)
    @Mapping(target = "paymentType", ignore = true)
    PartyEntryEntity toEntity(PartyEntryRequest request);

    @Mapping(target = "unit", ignore = true)
    @Mapping(target = "partyEntry", ignore = true)
    PartyUnitConfigurationEntity toEntity(PartyUnitConfigurationRequest request);

    @Mapping(source = "businessHead.headId", target = "businessHeadId")
    @Mapping(source = "businessHead.headName", target = "businessHeadName")
    @Mapping(source = "paymentType.id", target = "paymentTypeId")
    @Mapping(source = "paymentType.name", target = "paymentTypeName")
    PartyEntryResponse toResponse(PartyEntryEntity entity);

    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "unit.name", target = "unitName")
    PartyUnitConfigurationResponse toResponse(PartyUnitConfigurationEntity entity);

    @Mapping(source = "clientId", target = "id")
    @Mapping(source = "clientName", target = "name")
    PartyEntryResponse.ClientResponseDto toClientResponse(ClientEntity entity);

    @Mapping(source = "siteId", target = "id")
    @Mapping(source = "siteName", target = "name")
    PartyEntryResponse.SiteResponseDto toSiteResponse(SiteEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clients", ignore = true)
    @Mapping(target = "sites", ignore = true)
    @Mapping(target = "unitConfigurations", ignore = true)
    @Mapping(target = "businessHead", ignore = true)
    @Mapping(target = "paymentType", ignore = true)
    void updateEntity(PartyEntryRequest request, @MappingTarget PartyEntryEntity entity);
}
