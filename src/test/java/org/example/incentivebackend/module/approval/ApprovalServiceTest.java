package org.example.incentivebackend.module.approval;

import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.audit.util.AuditHelper;
import org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO;
import org.example.incentivebackend.module.approval.entity.*;
import org.example.incentivebackend.module.approval.enums.ApprovalActionType;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.repository.*;
import org.example.incentivebackend.module.approval.service.ApprovalServiceImpl;
import org.example.incentivebackend.module.master.designationpermission.service.DesignationPermissionService;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalWorkflowRepository workflowRepository;

    @Mock
    private ApprovalWorkflowLevelRepository workflowLevelRepository;

    @Mock
    private ApprovalWorkflowApproverRepository workflowApproverRepository;

    @Mock
    private ApprovalInstanceRepository instanceRepository;

    @Mock
    private ApprovalInstanceLevelRepository instanceLevelRepository;

    @Mock
    private ApprovalInstanceApproverRepository instanceApproverRepository;

    @Mock
    private ApprovalActionRepository actionRepository;

    @Mock
    private DesignationPermissionService designationPermissionService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditHelper auditHelper;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private UserEntity submitterUser;
    private UserEntity managerA;
    private UserEntity managerB;
    private UserEntity financeHead;
    private UserEntity directorUser;

    private ApprovalWorkflowEntity workflow;
    private ApprovalWorkflowLevelEntity level1Config;
    private ApprovalWorkflowLevelEntity level2Config;
    private ApprovalWorkflowLevelEntity level3Config;

    @BeforeEach
    void setUp() {
        submitterUser = UserEntity.builder().userId(1L).username("rahul_exec").fullName("Rahul").build();
        managerA = UserEntity.builder().userId(2L).username("manager_a").fullName("Manager A").build();
        managerB = UserEntity.builder().userId(3L).username("manager_b").fullName("Manager B").build();
        financeHead = UserEntity.builder().userId(4L).username("finance_head").fullName("Finance Head").build();
        directorUser = UserEntity.builder().userId(5L).username("director_user").fullName("Director").build();

        workflow = new ApprovalWorkflowEntity();
        workflow.setId(10L);
        workflow.setWorkflowName("PARTY_PAYMENT_APPROVAL");
        workflow.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        workflow.setIsActive(true);
        workflow.setRequireMakerChecker(true);

        level1Config = new ApprovalWorkflowLevelEntity();
        level1Config.setId(101L);
        level1Config.setWorkflow(workflow);
        level1Config.setLevelNumber(1);
        level1Config.setLevelName("Manager Approval");
        level1Config.setSequenceOrder(1);

        level2Config = new ApprovalWorkflowLevelEntity();
        level2Config.setId(102L);
        level2Config.setWorkflow(workflow);
        level2Config.setLevelNumber(2);
        level2Config.setLevelName("Finance Approval");
        level2Config.setSequenceOrder(2);

        level3Config = new ApprovalWorkflowLevelEntity();
        level3Config.setId(103L);
        level3Config.setWorkflow(workflow);
        level3Config.setLevelNumber(3);
        level3Config.setLevelName("Business Approval");
        level3Config.setSequenceOrder(3);
    }

    @Test
    void submit_Success_InitializesLevel1ActiveAndOthersPending() {
        when(workflowRepository.findByEntityTypeAndIsActiveTrue(WorkflowEntityType.PARTY_PAYMENT))
                .thenReturn(Optional.of(workflow));
        when(workflowLevelRepository.findByWorkflow_IdAndIsActiveTrueOrderBySequenceOrderAscLevelNumberAsc(10L))
                .thenReturn(List.of(level1Config, level2Config, level3Config));

        ApprovalWorkflowApproverEntity app1 = new ApprovalWorkflowApproverEntity();
        app1.setUser(managerA);
        ApprovalWorkflowApproverEntity app2 = new ApprovalWorkflowApproverEntity();
        app2.setUser(managerB);
        when(workflowApproverRepository.findByWorkflowLevel_IdAndIsActiveTrue(101L))
                .thenReturn(List.of(app1, app2));

        ApprovalWorkflowApproverEntity app3 = new ApprovalWorkflowApproverEntity();
        app3.setUser(financeHead);
        when(workflowApproverRepository.findByWorkflowLevel_IdAndIsActiveTrue(102L))
                .thenReturn(List.of(app3));

        ApprovalWorkflowApproverEntity app4 = new ApprovalWorkflowApproverEntity();
        app4.setUser(directorUser);
        when(workflowApproverRepository.findByWorkflowLevel_IdAndIsActiveTrue(103L))
                .thenReturn(List.of(app4));

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(instanceRepository.findTopByEntityTypeAndEntityIdOrderByAttemptNumberDesc(WorkflowEntityType.PARTY_PAYMENT, 1001L))
                .thenReturn(Optional.empty());

        when(designationPermissionService.hasPermission(1L, "PARTY_PAYMENT", "SUBMIT")).thenReturn(true);

        ApprovalInstanceEntity savedInstance = new ApprovalInstanceEntity();
        savedInstance.setId(500L);
        savedInstance.setWorkflow(workflow);
        savedInstance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        savedInstance.setEntityId(1001L);
        savedInstance.setCurrentLevelNumber(1);
        savedInstance.setStatus(ApprovalStatus.IN_PROGRESS);
        savedInstance.setSubmittedBy(submitterUser);
        savedInstance.setSubmittedAt(LocalDateTime.now());
        savedInstance.setAttemptNumber(1);
        when(instanceRepository.save(any(ApprovalInstanceEntity.class))).thenReturn(savedInstance);

        ApprovalInstanceLevelEntity instL1 = new ApprovalInstanceLevelEntity();
        instL1.setId(601L);
        instL1.setApprovalInstance(savedInstance);
        instL1.setLevelNumber(1);
        instL1.setLevelName("Manager Approval");
        instL1.setStatus(ApprovalStatus.ACTIVE);

        ApprovalInstanceLevelEntity instL2 = new ApprovalInstanceLevelEntity();
        instL2.setId(602L);
        instL2.setApprovalInstance(savedInstance);
        instL2.setLevelNumber(2);
        instL2.setLevelName("Finance Approval");
        instL2.setStatus(ApprovalStatus.PENDING);

        when(instanceLevelRepository.save(any(ApprovalInstanceLevelEntity.class)))
                .thenReturn(instL1, instL2);
        when(instanceLevelRepository.findByApprovalInstance_IdOrderByLevelNumberAsc(500L))
                .thenReturn(List.of(instL1, instL2));

        ApprovalDetailsDTO result = approvalService.submit(WorkflowEntityType.PARTY_PAYMENT, 1001L, submitterUser, "Please approve");

        assertNotNull(result);
        assertEquals(ApprovalStatus.IN_PROGRESS, result.getStatus());
        assertEquals(1, result.getCurrentLevelNumber());
        verify(actionRepository).save(argThat(action ->
                action.getAction() == ApprovalActionType.SUBMITTED && action.getAttemptNumber() == 1
        ));
    }

    @Test
    void approve_AnyOneUserAtLevel1_CompletesLevel1AndActivatesLevel2() {
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setId(500L);
        instance.setWorkflow(workflow);
        instance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        instance.setEntityId(1001L);
        instance.setCurrentLevelNumber(1);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submitterUser);
        instance.setAttemptNumber(1);

        ApprovalInstanceLevelEntity l1 = new ApprovalInstanceLevelEntity();
        l1.setId(601L);
        l1.setApprovalInstance(instance);
        l1.setLevelNumber(1);
        l1.setLevelName("Manager Approval");
        l1.setStatus(ApprovalStatus.ACTIVE);

        ApprovalInstanceLevelEntity l2 = new ApprovalInstanceLevelEntity();
        l2.setId(602L);
        l2.setApprovalInstance(instance);
        l2.setLevelNumber(2);
        l2.setLevelName("Finance Approval");
        l2.setStatus(ApprovalStatus.PENDING);

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.of(instance));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 1))
                .thenReturn(Optional.of(l1));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 2))
                .thenReturn(Optional.of(l2));

        when(designationPermissionService.hasPermission(managerA.getUserId(), "PARTY_PAYMENT", "APPROVE")).thenReturn(true);
        when(instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(601L, managerA.getUserId())).thenReturn(true);

        when(instanceLevelRepository.findByApprovalInstance_IdOrderByLevelNumberAsc(500L))
                .thenReturn(List.of(l1, l2));

        ApprovalDetailsDTO result = approvalService.approve(WorkflowEntityType.PARTY_PAYMENT, 1001L, managerA, "Manager A Approved");

        assertNotNull(result);
        assertEquals(ApprovalStatus.COMPLETED, l1.getStatus());
        assertEquals(managerA, l1.getCompletedBy());
        assertEquals(ApprovalStatus.ACTIVE, l2.getStatus());
        assertEquals(2, instance.getCurrentLevelNumber());
    }

    @Test
    void approve_SecondUserAtSameLevel_FailsDuplicateApproval() {
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setId(500L);
        instance.setWorkflow(workflow);
        instance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        instance.setEntityId(1001L);
        instance.setCurrentLevelNumber(1);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submitterUser);

        ApprovalInstanceLevelEntity l1 = new ApprovalInstanceLevelEntity();
        l1.setId(601L);
        l1.setApprovalInstance(instance);
        l1.setLevelNumber(1);
        l1.setStatus(ApprovalStatus.COMPLETED); // Already completed by managerA

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.of(instance));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 1))
                .thenReturn(Optional.of(l1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                approvalService.approve(WorkflowEntityType.PARTY_PAYMENT, 1001L, managerB, "Manager B approving again")
        );
        assertTrue(ex.getMessage().contains("already been completed"));
    }

    @Test
    void approve_FutureLevelUserEarlyApproval_FailsSecurityCheck() {
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setId(500L);
        instance.setWorkflow(workflow);
        instance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        instance.setEntityId(1001L);
        instance.setCurrentLevelNumber(1); // Level 1 is active
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submitterUser);

        ApprovalInstanceLevelEntity l1 = new ApprovalInstanceLevelEntity();
        l1.setId(601L);
        l1.setApprovalInstance(instance);
        l1.setLevelNumber(1);
        l1.setLevelName("Manager Approval");
        l1.setStatus(ApprovalStatus.ACTIVE);

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.of(instance));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 1))
                .thenReturn(Optional.of(l1));

        when(designationPermissionService.hasPermission(financeHead.getUserId(), "PARTY_PAYMENT", "APPROVE")).thenReturn(true);
        // financeHead is NOT assigned to Level 1
        when(instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(601L, financeHead.getUserId())).thenReturn(false);

        SecurityException ex = assertThrows(SecurityException.class, () ->
                approvalService.approve(WorkflowEntityType.PARTY_PAYMENT, 1001L, financeHead, "Finance Head approving early")
        );
        assertTrue(ex.getMessage().contains("not an assigned approver for Level 1"));
    }

    @Test
    void approve_MakerCheckerViolation_SubmitterCannotApproveOwnSubmission() {
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setId(500L);
        instance.setWorkflow(workflow);
        instance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        instance.setEntityId(1001L);
        instance.setCurrentLevelNumber(1);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submitterUser); // Submitted by rahul_exec

        ApprovalInstanceLevelEntity l1 = new ApprovalInstanceLevelEntity();
        l1.setId(601L);
        l1.setLevelNumber(1);
        l1.setStatus(ApprovalStatus.ACTIVE);

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.of(instance));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 1))
                .thenReturn(Optional.of(l1));

        SecurityException ex = assertThrows(SecurityException.class, () ->
                approvalService.approve(WorkflowEntityType.PARTY_PAYMENT, 1001L, submitterUser, "Approving own submission")
        );
        assertTrue(ex.getMessage().contains("Maker-checker violation"));
    }

    @Test
    void reject_HaltsWorkflow_MarksLevelAndInstanceRejected() {
        ApprovalInstanceEntity instance = new ApprovalInstanceEntity();
        instance.setId(500L);
        instance.setWorkflow(workflow);
        instance.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
        instance.setEntityId(1001L);
        instance.setCurrentLevelNumber(1);
        instance.setStatus(ApprovalStatus.IN_PROGRESS);
        instance.setSubmittedBy(submitterUser);

        ApprovalInstanceLevelEntity l1 = new ApprovalInstanceLevelEntity();
        l1.setId(601L);
        l1.setApprovalInstance(instance);
        l1.setLevelNumber(1);
        l1.setLevelName("Manager Approval");
        l1.setStatus(ApprovalStatus.ACTIVE);

        when(instanceRepository.findByEntityTypeAndEntityIdAndStatus(WorkflowEntityType.PARTY_PAYMENT, 1001L, ApprovalStatus.IN_PROGRESS))
                .thenReturn(Optional.of(instance));
        when(instanceLevelRepository.findByApprovalInstance_IdAndLevelNumber(500L, 1))
                .thenReturn(Optional.of(l1));

        when(designationPermissionService.hasPermission(managerA.getUserId(), "PARTY_PAYMENT", "REJECT")).thenReturn(true);
        when(instanceApproverRepository.existsByInstanceLevel_IdAndUser_UserId(601L, managerA.getUserId())).thenReturn(true);

        when(instanceLevelRepository.findByApprovalInstance_IdOrderByLevelNumberAsc(500L))
                .thenReturn(List.of(l1));

        ApprovalDetailsDTO result = approvalService.reject(WorkflowEntityType.PARTY_PAYMENT, 1001L, managerA, "Allocation incorrect");

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, l1.getStatus());
        assertEquals(ApprovalStatus.REJECTED, instance.getStatus());
        verify(actionRepository).save(argThat(action ->
                action.getAction() == ApprovalActionType.REJECTED && action.getRemarks().equals("Allocation incorrect")
        ));
    }

    @Test
    void reject_MissingReason_FailsValidation() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                approvalService.reject(WorkflowEntityType.PARTY_PAYMENT, 1001L, managerA, "   ")
        );
        assertTrue(ex.getMessage().contains("Rejection reason is mandatory"));
    }
}
