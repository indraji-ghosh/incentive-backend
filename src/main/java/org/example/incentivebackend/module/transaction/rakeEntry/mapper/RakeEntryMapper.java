package org.example.incentivebackend.module.transaction.rakeEntry.mapper;

import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeAnnexureRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeAnnexureResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RakeEntryMapper {

    @Mapping(target = "rakeEntryId", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "annexures", ignore = true)
    @Mapping(target = "rakeStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modNo", ignore = true)
    @Mapping(target = "modBy", ignore = true)
    @Mapping(target = "modAt", ignore = true)
    @Mapping(target = "appStatus", ignore = true)
    @Mapping(target = "appBy", ignore = true)
    @Mapping(target = "appAt", ignore = true)
    RakeEntryEntity toEntity(RakeEntryRequest request);

    @Mapping(target = "rakeEntryId", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "site", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "annexures", ignore = true)
    @Mapping(target = "rakeStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modNo", ignore = true)
    @Mapping(target = "modBy", ignore = true)
    @Mapping(target = "modAt", ignore = true)
    @Mapping(target = "appStatus", ignore = true)
    @Mapping(target = "appBy", ignore = true)
    @Mapping(target = "appAt", ignore = true)
    void updateEntityFromRequest(RakeEntryRequest request, @MappingTarget RakeEntryEntity entity);

    @Mapping(source = "client.clientId", target = "clientId")
    @Mapping(source = "client.clientName", target = "clientName")
    @Mapping(source = "client.clientShortCode", target = "clientShortCode")
    @Mapping(source = "party.id", target = "partyId")
    @Mapping(source = "party.partyName", target = "partyName")
    @Mapping(source = "site.siteId", target = "siteId")
    @Mapping(source = "site.siteName", target = "siteName")
    @Mapping(source = "site.siteShortCode", target = "siteShortCode")
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "serviceName", ignore = true)
    @Mapping(target = "totalWagons", ignore = true)
    @Mapping(target = "totalWeight", ignore = true)
    RakeEntryResponse toResponse(RakeEntryEntity entity);

    @AfterMapping
    default void populateComputedFields(RakeEntryEntity entity, @MappingTarget RakeEntryResponse.RakeEntryResponseBuilder response) {
        if (entity.getService() != null) {
            response.serviceId(entity.getService().getId());
            response.serviceName(entity.getService().getName());
        }
        if (entity.getAnnexures() != null) {
            int wagons = entity.getAnnexures().stream()
                    .mapToInt(a -> a.getWagons() != null ? a.getWagons() : 0)
                    .sum();
            response.totalWagons(wagons);

            BigDecimal weight = entity.getAnnexures().stream()
                    .map(a -> a.getWeight() != null ? a.getWeight() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.totalWeight(weight);
        }
    }

    @Mapping(target = "rakeAnnexureId", ignore = true)
    @Mapping(target = "rakeEntry", ignore = true)
    RakeAnnexureEntity toAnnexureEntity(RakeAnnexureRequest request);

    @Mapping(target = "rakeAnnexureId", ignore = true)
    @Mapping(target = "rakeEntry", ignore = true)
    void updateAnnexureEntityFromRequest(RakeAnnexureRequest request, @MappingTarget RakeAnnexureEntity entity);

    RakeAnnexureResponse toAnnexureResponse(RakeAnnexureEntity entity);

    List<RakeEntryResponse> toResponseList(List<RakeEntryEntity> entities);
    List<RakeAnnexureResponse> toAnnexureResponseList(List<RakeAnnexureEntity> entities);
}
