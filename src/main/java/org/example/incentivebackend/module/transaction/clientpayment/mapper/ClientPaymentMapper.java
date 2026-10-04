package org.example.incentivebackend.module.transaction.clientpayment.mapper;

import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.ClientPaymentResponse;
import org.example.incentivebackend.module.transaction.clientpayment.entity.ClientPaymentEntity;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ClientPaymentMapper {

    public ClientPaymentEntity toEntity(ClientPaymentRequest request) {
        if (request == null) {
            return null;
        }

        ClientPaymentEntity entity = new ClientPaymentEntity();
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setPaymentDate(request.getPaymentDate());
        entity.setRemarks(request.getRemarks());

        return entity;
    }

    public void updateEntityFromRequest(ClientPaymentRequest request, ClientPaymentEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setPaymentDate(request.getPaymentDate());
        entity.setRemarks(request.getRemarks());
    }

    public ClientPaymentResponse toResponse(ClientPaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        ClientPaymentResponse response = new ClientPaymentResponse();
        response.setClientPaymentId(entity.getClientPaymentId());
        response.setPaymentNo(entity.getPaymentNo());
        
        if (entity.getBill() != null) {
            response.setBillId(entity.getBill().getBillId());
            response.setBillNo(entity.getBill().getBillNumber());
        }
        
        if (entity.getClient() != null) {
            response.setClientId(entity.getClient().getClientId());
            response.setClientName(entity.getClient().getClientName());
        }
        
        response.setPaymentAmount(entity.getPaymentAmount());
        response.setPaymentDate(entity.getPaymentDate());
        response.setRemarks(entity.getRemarks());

        if (entity.getCreatedAt() != null) {
            response.setCreatedAt(entity.getCreatedAt().toString());
        }
        if (entity.getCreatedAt() != null) {
            response.setUpdatedAt(entity.getCreatedAt().toString());
        }

        return response;
    }
}
