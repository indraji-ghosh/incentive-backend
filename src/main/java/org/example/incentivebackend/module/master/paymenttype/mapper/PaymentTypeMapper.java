package org.example.incentivebackend.module.master.paymenttype.mapper;

import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentTypeMapper {
    PaymentTypeEntity toEntity(PaymentTypeRequest request);
    PaymentTypeResponse toResponse(PaymentTypeEntity entity);
    List<PaymentTypeResponse> toResponseList(List<PaymentTypeEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // code should not be updated
    void updateEntity(PaymentTypeRequest request, @MappingTarget PaymentTypeEntity entity);
}
