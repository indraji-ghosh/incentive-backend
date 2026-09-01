package org.example.incentivebackend.module.transaction.commissionpayment.mapper;

import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommissionPaymentMapper {

    @Mapping(source = "party.id", target = "partyId")
    @Mapping(source = "party.partyName", target = "partyName")
    CommissionPaymentResponse toResponse(CommissionPaymentEntity entity);
}
