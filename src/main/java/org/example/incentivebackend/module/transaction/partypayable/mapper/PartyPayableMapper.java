package org.example.incentivebackend.module.transaction.partypayable.mapper;

import org.example.incentivebackend.module.transaction.partypayable.dto.response.PartyPayableResponse;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PartyPayableMapper {

    @Mapping(target = "partyId", source = "party.id")
    @Mapping(target = "partyName", source = "party.partyName")
    @Mapping(target = "partyAssignmentId", source = "partyAssignment.id")
    @Mapping(target = "clientId", source = "partyAssignment.client.clientId")
    @Mapping(target = "clientName", source = "partyAssignment.client.clientName")
    @Mapping(target = "siteId", source = "partyAssignment.site.siteId")
    @Mapping(target = "siteName", source = "partyAssignment.site.siteName")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    PartyPayableResponse toResponse(PartyPayableEntity entity);

    List<PartyPayableResponse> toResponseList(List<PartyPayableEntity> entities);
}
