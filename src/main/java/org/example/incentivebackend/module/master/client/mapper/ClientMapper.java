package org.example.incentivebackend.module.master.client.mapper;


import org.example.incentivebackend.module.master.client.dto.ClientRequest;
import org.example.incentivebackend.module.master.client.dto.ClientResponse;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.mapstruct.*;

        import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClientMapper {

    ClientEntity toEntity(ClientRequest request);

    ClientResponse toResponse(ClientEntity entity);

    List<ClientResponse> toResponseList(
            List<ClientEntity> entities
    );

    @Mapping(target = "clientId", ignore = true)
    void updateEntity(
            ClientRequest request,
            @MappingTarget ClientEntity entity
    );
}
