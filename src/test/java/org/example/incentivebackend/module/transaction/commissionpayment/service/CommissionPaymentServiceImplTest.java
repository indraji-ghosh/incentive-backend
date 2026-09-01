package org.example.incentivebackend.module.transaction.commissionpayment.service;

import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.mapper.CommissionPaymentMapper;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionPaymentServiceImplTest {

    @Mock
    private CommissionPaymentRepository repository;

    @Mock
    private PartyEntryRepository partyEntryRepository;

    @Mock
    private CommissionCalculationService commissionCalculationService;

    @Mock
    private CommissionPaymentMapper mapper;

    @InjectMocks
    private CommissionPaymentServiceImpl service;

    private PartyEntryEntity mockParty;

    @BeforeEach
    void setUp() {
        mockParty = new PartyEntryEntity();
        mockParty.setId(1L);
        mockParty.setPartyName("Test Party");
    }

    @Test
    void createPayment_ShouldCreatePayment() {
        CommissionPaymentRequest request = new CommissionPaymentRequest();
        request.setPartyId(1L);
        request.setPaymentDate(LocalDate.now());
        request.setPaymentAmount(new BigDecimal("50000.00"));

        when(partyEntryRepository.findById(1L)).thenReturn(Optional.of(mockParty));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));
        when(repository.sumActivePaymentAmountByPartyId(1L)).thenReturn(new BigDecimal("0.00"));
        
        CommissionPaymentEntity savedEntity = new CommissionPaymentEntity();
        savedEntity.setCommissionPaymentId(10L);
        when(repository.save(any(CommissionPaymentEntity.class))).thenReturn(savedEntity);

        assertDoesNotThrow(() -> service.create(request));
    }

    @Test
    void createPayment_WhenCommissionExceedsOutstanding_ShouldThrowException() {
        CommissionPaymentRequest request = new CommissionPaymentRequest();
        request.setPartyId(1L);
        request.setPaymentAmount(new BigDecimal("60000.00"));

        when(partyEntryRepository.findById(1L)).thenReturn(Optional.of(mockParty));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));
        when(repository.sumActivePaymentAmountByPartyId(1L)).thenReturn(new BigDecimal("100000.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> service.create(request));
        assertTrue(e.getMessage().contains("exceeds outstanding"));
    }
}
