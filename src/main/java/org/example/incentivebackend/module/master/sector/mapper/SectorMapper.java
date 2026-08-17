package org.example.incentivebackend.module.master.sector.mapper;

import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.example.incentivebackend.module.master.sector.entity.SectorEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SectorMapper {

    SectorEntity toEntity(SectorRequest request);

    SectorResponse toResponse(SectorEntity entity);

    List<SectorResponse> toResponseList(
            List<SectorEntity> entities
    );

    @Mapping(target = "sectorId", ignore = true)
    void updateEntity(
            SectorRequest request,
            @MappingTarget SectorEntity entity
    );
}