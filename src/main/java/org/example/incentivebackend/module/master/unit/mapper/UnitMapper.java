package org.example.incentivebackend.module.master.unit.mapper;

import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UnitMapper {
    UnitEntity toEntity(UnitCreateRequest request);
    UnitResponse toResponse(UnitEntity entity);
    List<UnitResponse> toResponseList(List<UnitEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // code should not be updated
    void updateEntity(UnitUpdateRequest request, @MappingTarget UnitEntity entity);
}
