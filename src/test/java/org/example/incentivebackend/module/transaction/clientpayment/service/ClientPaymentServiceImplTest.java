package org.example.incentivebackend.module.transaction.clientpayment.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.BusinessValidationException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.ClientPaymentResponse;
import org.example.incentivebackend.module.transaction.clientpayment.entity.ClientPaymentEntity;
import org.example.incentivebackend.module.transaction.clientpayment.mapper.ClientPaymentMapper;
import org.example.incentivebackend.module.transaction.clientpayment.repository.ClientPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientPaymentServiceImplTest {

    @Mock
    private ClientPaymentRepository clientPaymentRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private ClientPaymentMapper clientPaymentMapper;

    @InjectMocks
    private ClientPaymentServiceImpl clientPaymentService;

    private BillEntity bill;
    private ClientEntity client;

    @BeforeEach
    void setUp() {
        client = new ClientEntity();
        client.setClientId(1L);
        client.setClientName("Test Client");

        bill = new BillEntity();
        bill.setBillId(1L);
        bill.setBillNumber("BILL-001");
        bill.setBillAmount(new BigDecimal("100000.00"));
        bill.setClient(client);
    }

    @Test
    void testCreateFirstPayment_PartiallyPaid() {
        ClientPaymentRequest request = new ClientPaymentRequest();
        request.setBillId(1L);
        request.setPaymentAmount(new BigDecimal("30000.00"));

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(clientPaymentRepository.getTotalPaidAmountByBillId(1L, StatusEnum.A)).thenReturn(BigDecimal.ZERO);
        
        ClientPaymentEntity savedEntity = new ClientPaymentEntity();
        when(clientPaymentMapper.toEntity(request)).thenReturn(new ClientPaymentEntity());
        when(clientPaymentRepository.save(any())).thenReturn(savedEntity);
        when(clientPaymentMapper.toResponse(any())).thenReturn(new ClientPaymentResponse());

        clientPaymentService.createPayment(request);

        verify(clientPaymentRepository).save(any(ClientPaymentEntity.class));
    }

    @Test
    void testCreateMultiplePayments_FullyPaid() {
        ClientPaymentRequest request = new ClientPaymentRequest();
        request.setBillId(1L);
        request.setPaymentAmount(new BigDecimal("50000.00"));

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(clientPaymentRepository.getTotalPaidAmountByBillId(1L, StatusEnum.A)).thenReturn(new BigDecimal("50000.00"));
        
        when(clientPaymentMapper.toEntity(request)).thenReturn(new ClientPaymentEntity());
        when(clientPaymentRepository.save(any())).thenReturn(new ClientPaymentEntity());
        when(clientPaymentMapper.toResponse(any())).thenReturn(new ClientPaymentResponse());

        clientPaymentService.createPayment(request);

        verify(clientPaymentRepository).save(any(ClientPaymentEntity.class));
    }

    @Test
    void testOverpaymentValidation() {
        ClientPaymentRequest request = new ClientPaymentRequest();
        request.setBillId(1L);
        request.setPaymentAmount(new BigDecimal("30000.00"));

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(clientPaymentRepository.getTotalPaidAmountByBillId(1L, StatusEnum.A)).thenReturn(new BigDecimal("80000.00"));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> clientPaymentService.createPayment(request));
        assertTrue(ex.getMessage().contains("exceeds outstanding"));
    }

    @Test
    void testUpdatePayment_Valid() {
        ClientPaymentRequest request = new ClientPaymentRequest();
        request.setPaymentAmount(new BigDecimal("70000.00"));

        ClientPaymentEntity payment = new ClientPaymentEntity();
        payment.setClientPaymentId(10L);
        payment.setBill(bill);
        payment.setPaymentAmount(new BigDecimal("40000.00"));

        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(clientPaymentRepository.getTotalPaidAmountByBillIdExcluding(1L, 10L, StatusEnum.A)).thenReturn(new BigDecimal("30000.00"));
        when(clientPaymentRepository.save(any())).thenReturn(payment);

        clientPaymentService.updatePayment(10L, request);

        verify(clientPaymentMapper).updateEntityFromRequest(request, payment);
        verify(clientPaymentRepository).save(payment);
    }

    @Test
    void testUpdatePayment_Invalid() {
        ClientPaymentRequest request = new ClientPaymentRequest();
        request.setPaymentAmount(new BigDecimal("80000.00"));

        ClientPaymentEntity payment = new ClientPaymentEntity();
        payment.setClientPaymentId(10L);
        payment.setBill(bill);
        payment.setPaymentAmount(new BigDecimal("40000.00"));

        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(clientPaymentRepository.getTotalPaidAmountByBillIdExcluding(1L, 10L, StatusEnum.A)).thenReturn(new BigDecimal("30000.00"));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> clientPaymentService.updatePayment(10L, request));
        assertTrue(ex.getMessage().contains("exceeds outstanding"));
    }

    @Test
    void testSoftDelete() {
        ClientPaymentEntity payment = new ClientPaymentEntity();
        payment.setClientPaymentId(10L);
        when(clientPaymentRepository.findById(10L)).thenReturn(Optional.of(payment));

        clientPaymentService.deletePayment(10L);

        assertEquals(StatusEnum.I, payment.getPaymentStatus());
        verify(clientPaymentRepository).save(payment);
    }

    @Test
    void testBillPaymentSummary() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(clientPaymentRepository.getTotalPaidAmountByBillId(1L, StatusEnum.A)).thenReturn(new BigDecimal("40000.00"));

        BillPaymentSummaryResponse response = clientPaymentService.getBillPaymentSummary(1L);

        assertEquals(0, new BigDecimal("60000.00").compareTo(response.getOutstandingAmount()));
        assertEquals("PARTIALLY_PAID", response.getPaymentStatus());
    }
}
