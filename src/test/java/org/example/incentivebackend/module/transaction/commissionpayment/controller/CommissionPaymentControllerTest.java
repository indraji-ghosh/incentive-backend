package org.example.incentivebackend.module.transaction.commissionpayment.controller;

import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommissionPaymentControllerTest {

    @Mock
    private CommissionPaymentService service;

    @InjectMocks
    private CommissionPaymentController controller;

    @Test
    void create_ShouldReturnCreated() {
        CommissionPaymentRequest request = new CommissionPaymentRequest();
        request.setPartyId(1L);
        request.setPaymentDate(LocalDate.now());
        request.setPaymentAmount(new BigDecimal("50000.00"));

        CommissionPaymentResponse response = new CommissionPaymentResponse();
        response.setCommissionPaymentId(100L);
        
        when(service.create(any())).thenReturn(response);

        ResponseEntity<?> result = controller.create(request);
        assertEquals(201, result.getStatusCode().value());
    }
}
