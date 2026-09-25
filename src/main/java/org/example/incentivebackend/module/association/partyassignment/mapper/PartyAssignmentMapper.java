package org.example.incentivebackend.module.association.partyassignment.mapper;

import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyAssignmentResponse;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyServiceConfigurationResponse;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PartyAssignmentMapper {

    @Mapping(target = "partyId", source = "party.id")
    @Mapping(target = "partyName", source = "party.partyName")
    @Mapping(target = "clientId", source = "client.clientId")
    @Mapping(target = "clientName", source = "client.clientName")
    @Mapping(target = "clientShortCode", source = "client.clientShortCode")
    @Mapping(target = "siteId", source = "site.siteId")
    @Mapping(target = "siteName", source = "site.siteName")
    @Mapping(target = "siteShortCode", source = "site.siteShortCode")
    @Mapping(target = "serviceConfigurations", source = "serviceConfigurations")
    PartyAssignmentResponse toResponse(PartyAssignmentEntity entity);

    List<PartyAssignmentResponse> toResponseList(List<PartyAssignmentEntity> entities);

    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "paymentTypeId", source = "paymentType.id")
    @Mapping(target = "paymentTypeCode", source = "paymentType.code")
    @Mapping(target = "paymentTypeName", source = "paymentType.name")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitCode", source = "unit.code")
    @Mapping(target = "unitName", source = "unit.name")
    PartyServiceConfigurationResponse toServiceConfigResponse(PartyServiceConfigurationEntity entity);

    List<PartyServiceConfigurationResponse> toServiceConfigResponseList(List<PartyServiceConfigurationEntity> entities);
}
