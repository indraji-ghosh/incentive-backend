package org.example.incentivebackend.module.transaction.commissionpayment;

import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.audit.util.AuditHelper;
import org.example.incentivebackend.common.enums.PaymentStatusEnum;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.service.ApprovalService;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.PartyPaymentAdjustmentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.mapper.CommissionPaymentMapper;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionCalculationService;
import org.example.incentivebackend.module.transaction.commissionpayment.service.CommissionPaymentServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyPaymentApprovalFlowTest {

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
    private UserEntity execRahul;
    private UserEntity managerA;
    private UserEntity finHead;
    private UserEntity bizHead;
    private PartyPayableEntity mockPayable;

    @BeforeEach
    void setUp() {
        mockParty = new PartyEntity();
        mockParty.setId(10L);
        mockParty.setPartyName("Express Logistics");

        execRahul = UserEntity.builder().userId(1L).username("rahul_exec").fullName("Rahul").build();
        managerA = UserEntity.builder().userId(2L).username("manager_a").fullName("Manager A").build();
        finHead = UserEntity.builder().userId(4L).username("finance_head").fullName("Finance Head").build();
        bizHead = UserEntity.builder().userId(6L).username("biz_head").fullName("Business Head").build();

        mockPayable = new PartyPayableEntity();
        mockPayable.setId(201L);
        mockPayable.setParty(mockParty);
        mockPayable.setStatus(StatusEnum.A);
        mockPayable.setPayableAmount(new BigDecimal("100000.00"));
        mockPayable.setPaidAmount(BigDecimal.ZERO);
        mockPayable.setOutstandingAmount(new BigDecimal("100000.00"));
        mockPayable.setPaymentStatus(PaymentStatusEnum.UNPAID);
    }

    @Test
    void testFullApprovalLifecycle_FromDraftToPaid() {
        // Step 1: Create as DRAFT
        CommissionPaymentRequest createReq = new CommissionPaymentRequest();
        createReq.setPartyId(10L);
        createReq.setPaymentDate(LocalDate.now());
        createReq.setPaymentAmount(new BigDecimal("50000.00"));
        CommissionPaymentRequest.PaymentAllocationRequest alloc = new CommissionPaymentRequest.PaymentAllocationRequest();
        alloc.setPayableId(201L);
        alloc.setAmount(new BigDecimal("50000.00"));
        createReq.setAllocations(List.of(alloc));

        when(partyRepository.findById(10L)).thenReturn(Optional.of(mockParty));
        when(commissionCalculationService.getTotalCommissionEarned(10L)).thenReturn(new BigDecimal("100000.00"));
        when(repository.sumActivePayablePaymentAmountByPartyId(10L)).thenReturn(BigDecimal.ZERO);
        when(repository.sumAdjustedAdvanceAmountByPartyId(10L)).thenReturn(BigDecimal.ZERO);
        when(partyPayableRepository.findById(201L)).thenReturn(Optional.of(mockPayable));

        CommissionPaymentEntity paymentEntity = new CommissionPaymentEntity();
        paymentEntity.setCommissionPaymentId(999L);
        paymentEntity.setPaymentNo("PP-999");
        paymentEntity.setStatus("DRAFT");
        paymentEntity.setPaymentAmount(new BigDecimal("50000.00"));
        paymentEntity.setPaymentType("PAYABLE_PAYMENT");
        paymentEntity.setParty(mockParty);

        when(repository.save(any(CommissionPaymentEntity.class))).thenReturn(paymentEntity);

        CommissionPaymentResponse draftResp = new CommissionPaymentResponse();
        draftResp.setCommissionPaymentId(999L);
        draftResp.setStatus("DRAFT");
        when(mapper.toResponse(paymentEntity)).thenReturn(draftResp);

        CommissionPaymentResponse created = service.create(createReq);
        assertEquals("DRAFT", created.getStatus());

        // Verify that financial posting was NOT performed
        verify(partyPayableRepository, never()).save(any(PartyPayableEntity.class));

        // Step 2: Submit payment
        when(repository.findById(999L)).thenReturn(Optional.of(paymentEntity));
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(execRahul);

        ApprovalDetailsDTO approvalProgress = ApprovalDetailsDTO.builder()
                .status(ApprovalStatus.IN_PROGRESS)
                .currentLevelNumber(1)
                .currentLevelName("Manager Approval")
                .build();
        when(approvalService.submit(eq(WorkflowEntityType.PARTY_PAYMENT), eq(999L), eq(execRahul), any()))
                .thenReturn(approvalProgress);

        CommissionPaymentResponse submitted = service.submit(999L, "Please review");
        assertEquals("PENDING_APPROVAL", paymentEntity.getStatus());
        assertEquals(execRahul.getUserId(), paymentEntity.getSubmittedBy());

        // Step 3: Level 1 Approval (Manager A)
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(managerA);
        ApprovalDetailsDTO l1Approved = ApprovalDetailsDTO.builder()
                .status(ApprovalStatus.IN_PROGRESS)
                .currentLevelNumber(2)
                .currentLevelName("Finance Approval")
                .build();
        when(approvalService.approve(eq(WorkflowEntityType.PARTY_PAYMENT), eq(999L), eq(managerA), any()))
                .thenReturn(l1Approved);

        service.approve(999L, "Manager A Approved");
        assertEquals("PENDING_APPROVAL", paymentEntity.getStatus());

        // Step 4: Level 2 Approval (Finance Head)
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(finHead);
        ApprovalDetailsDTO l2Approved = ApprovalDetailsDTO.builder()
                .status(ApprovalStatus.IN_PROGRESS)
                .currentLevelNumber(3)
                .currentLevelName("Business Approval")
                .build();
        when(approvalService.approve(eq(WorkflowEntityType.PARTY_PAYMENT), eq(999L), eq(finHead), any()))
                .thenReturn(l2Approved);

        service.approve(999L, "Finance Head Approved");
        assertEquals("PENDING_APPROVAL", paymentEntity.getStatus());

        // Step 5: Level 3 Approval (Business Head - Final)
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(bizHead);
        ApprovalDetailsDTO l3Approved = ApprovalDetailsDTO.builder()
                .status(ApprovalStatus.APPROVED)
                .currentLevelNumber(3)
                .build();
        when(approvalService.approve(eq(WorkflowEntityType.PARTY_PAYMENT), eq(999L), eq(bizHead), any()))
                .thenReturn(l3Approved);

        service.approve(999L, "Business Head Final Approved");
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertNotNull(paymentEntity.getApprovedAt());

        // Step 6: Process Payment Disbursement (APPROVED -> PAID)
        PartyPaymentAdjustmentEntity adjustment = new PartyPaymentAdjustmentEntity();
        adjustment.setCommissionPayment(paymentEntity);
        adjustment.setPayable(mockPayable);
        adjustment.setAdjustedAmount(new BigDecimal("50000.00"));
        when(adjustmentRepository.findByCommissionPayment_CommissionPaymentId(999L))
                .thenReturn(List.of(adjustment));
        when(partyPayableRepository.findById(201L)).thenReturn(Optional.of(mockPayable));

        service.pay(999L);
        assertEquals("PAID", paymentEntity.getStatus());

        // Now verify financial posting executed correctly!
        verify(partyPayableRepository, atLeastOnce()).save(mockPayable);
        assertEquals(new BigDecimal("50000.00"), mockPayable.getPaidAmount());
        assertEquals(new BigDecimal("50000.00"), mockPayable.getOutstandingAmount());
        assertEquals(PaymentStatusEnum.PARTIALLY_PAID, mockPayable.getPaymentStatus());
    }

    @Test
    void testRejection_HaltsWorkflow_MarksRejected() {
        CommissionPaymentEntity paymentEntity = new CommissionPaymentEntity();
        paymentEntity.setCommissionPaymentId(999L);
        paymentEntity.setStatus("PENDING_APPROVAL");
        paymentEntity.setParty(mockParty);

        when(repository.findById(999L)).thenReturn(Optional.of(paymentEntity));
        when(repository.save(any(CommissionPaymentEntity.class))).thenReturn(paymentEntity);
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(managerA);

        ApprovalDetailsDTO rejectedDto = ApprovalDetailsDTO.builder()
                .status(ApprovalStatus.REJECTED)
                .build();
        when(approvalService.reject(eq(WorkflowEntityType.PARTY_PAYMENT), eq(999L), eq(managerA), eq("Allocation error")))
                .thenReturn(rejectedDto);

        CommissionPaymentResponse response = new CommissionPaymentResponse();
        response.setStatus("REJECTED");
        when(mapper.toResponse(any())).thenReturn(response);

        service.reject(999L, "Allocation error");
        assertEquals("REJECTED", paymentEntity.getStatus());
        assertEquals("Allocation error", paymentEntity.getRejectionReason());
        assertEquals(managerA.getUserId(), paymentEntity.getRejectedBy());
    }
}
