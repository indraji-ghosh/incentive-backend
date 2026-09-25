package org.example.incentivebackend.module.master.party.mapper;

import org.example.incentivebackend.module.master.party.dto.request.PartyRequest;
import org.example.incentivebackend.module.master.party.dto.response.PartyResponse;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PartyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessHead", ignore = true)
    PartyEntity toEntity(PartyRequest request);

    @Mapping(target = "businessHeadId", source = "businessHead.headId")
    @Mapping(target = "businessHeadName", source = "businessHead.headName")
    PartyResponse toResponse(PartyEntity entity);

    List<PartyResponse> toResponseList(List<PartyEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "businessHead", ignore = true)
    void updateEntity(PartyRequest request, @MappingTarget PartyEntity entity);
}
