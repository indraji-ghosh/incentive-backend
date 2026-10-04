package org.example.incentivebackend.module.transaction.commissionpayment.service;

import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.audit.util.AuditHelper;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.approval.service.ApprovalService;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.mapper.CommissionPaymentMapper;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionPaymentServiceImplTest {

    @Mock
    private CommissionPaymentRepository repository;

    @Mock
    private PartyPaymentAdjustmentRepository adjustmentRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private CommissionCalculationService commissionCalculationService;

    @Mock
    private CommissionPaymentMapper mapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private PartyPayableRepository partyPayableRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditHelper auditHelper;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommissionPaymentServiceImpl service;

    private PartyEntity mockParty;

    @BeforeEach
    void setUp() {
        mockParty = new PartyEntity();
        mockParty.setId(1L);
        mockParty.setPartyName("Test Party");
    }

    @Test
    void createPayment_ShouldCreatePaymentInDraftStatus() {
        CommissionPaymentRequest request = new CommissionPaymentRequest();
        request.setPartyId(1L);
        request.setPaymentDate(LocalDate.now());
        request.setPaymentAmount(new BigDecimal("50000.00"));

        CommissionPaymentRequest.PaymentAllocationRequest alloc = new CommissionPaymentRequest.PaymentAllocationRequest();
        alloc.setPayableId(100L);
        alloc.setAmount(new BigDecimal("50000.00"));
        request.setAllocations(List.of(alloc));

        when(partyRepository.findById(1L)).thenReturn(Optional.of(mockParty));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));
        when(repository.sumActivePayablePaymentAmountByPartyId(1L)).thenReturn(new BigDecimal("0.00"));
        when(repository.sumAdjustedAdvanceAmountByPartyId(1L)).thenReturn(new BigDecimal("0.00"));

        PartyPayableEntity mockPayable = new PartyPayableEntity();
        mockPayable.setId(100L);
        mockPayable.setParty(mockParty);
        mockPayable.setStatus(StatusEnum.A);
        mockPayable.setPayableAmount(new BigDecimal("50000.00"));
        mockPayable.setOutstandingAmount(new BigDecimal("50000.00"));
        when(partyPayableRepository.findById(100L)).thenReturn(Optional.of(mockPayable));

        CommissionPaymentEntity savedEntity = new CommissionPaymentEntity();
        savedEntity.setCommissionPaymentId(10L);
        savedEntity.setPaymentNo("PP-TEST1234");
        savedEntity.setStatus("DRAFT");
        when(repository.save(any(CommissionPaymentEntity.class))).thenReturn(savedEntity);

        CommissionPaymentResponse mockResponse = new CommissionPaymentResponse();
        mockResponse.setCommissionPaymentId(10L);
        mockResponse.setStatus("DRAFT");
        when(mapper.toResponse(any(CommissionPaymentEntity.class))).thenReturn(mockResponse);

        CommissionPaymentResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("DRAFT", response.getStatus());
        // Verify payables were NOT altered during DRAFT creation
        verify(partyPayableRepository, never()).save(any(PartyPayableEntity.class));
    }

    @Test
    void createPayment_WhenCommissionExceedsOutstanding_ShouldThrowException() {
        CommissionPaymentRequest request = new CommissionPaymentRequest();
        request.setPartyId(1L);
        request.setPaymentAmount(new BigDecimal("60000.00"));

        when(partyRepository.findById(1L)).thenReturn(Optional.of(mockParty));
        when(commissionCalculationService.getTotalCommissionEarned(1L)).thenReturn(new BigDecimal("150000.00"));
        when(repository.sumActivePayablePaymentAmountByPartyId(1L)).thenReturn(new BigDecimal("100000.00"));
        when(repository.sumAdjustedAdvanceAmountByPartyId(1L)).thenReturn(new BigDecimal("0.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> service.create(request));
        assertTrue(e.getMessage().contains("exceeds outstanding"));
    }
}
