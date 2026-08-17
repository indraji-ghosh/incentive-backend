package org.example.incentivebackend.module.master.site.mapper;

import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SiteMapper {

    SiteEntity toEntity(SiteRequest request);

    SiteResponse toResponse(SiteEntity entity);

    List<SiteResponse> toResponseList(
            List<SiteEntity> entities
    );

    @Mapping(target = "siteId", ignore = true)
    void updateEntity(
            SiteRequest request,
            @MappingTarget SiteEntity entity
    );
}