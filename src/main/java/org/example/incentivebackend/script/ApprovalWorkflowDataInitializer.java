package org.example.incentivebackend.script;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowApproverEntity;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowEntity;
import org.example.incentivebackend.module.approval.entity.ApprovalWorkflowLevelEntity;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowApproverRepository;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowLevelRepository;
import org.example.incentivebackend.module.approval.repository.ApprovalWorkflowRepository;
import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.example.incentivebackend.module.master.designation.repository.DesignationRepository;
import org.example.incentivebackend.module.master.designationpermission.entity.DesignationPagePermissionEntity;
import org.example.incentivebackend.module.master.designationpermission.repository.DesignationPagePermissionRepository;
import org.example.incentivebackend.module.master.page.entity.PageEntity;
import org.example.incentivebackend.module.master.page.repository.PageRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.master.user.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApprovalWorkflowDataInitializer implements CommandLineRunner {

    private final ApprovalWorkflowRepository workflowRepository;
    private final ApprovalWorkflowLevelRepository levelRepository;
    private final ApprovalWorkflowApproverRepository approverRepository;
    private final UserRepository userRepository;
    private final DesignationRepository designationRepository;
    private final PageRepository pageRepository;
    private final DesignationPagePermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing Party Payment Approval Workflow data...");

        // 1. Ensure PARTY_PAYMENT page exists in mm_page
        PageEntity page = ensurePage("PARTY_PAYMENT", "Party Payment", "Operations", "/operations/party-payments", "Wallet", 15);

        // 2. Ensure Designations exist
        DesignationEntity desigManager = ensureDesignation("MGR", "Operations Manager", "L1");
        DesignationEntity desigFinance = ensureDesignation("FIN", "Finance & Accounts", "L2");
        DesignationEntity desigDirector = ensureDesignation("DIR", "Director & Leadership", "L3");
        DesignationEntity desigExecutive = ensureDesignation("EXEC", "Payment Executive", "L0");

        // 3. Ensure Users exist for test / multi-user workflow
        UserEntity execRahul = ensureUser("rahul_exec", "Rahul Sharma", "rahul@incentive.com", desigExecutive);
        UserEntity mgrA = ensureUser("manager_a", "Manager A (Amit)", "amit@incentive.com", desigManager);
        UserEntity mgrB = ensureUser("manager_b", "Manager B (Bhavna)", "bhavna@incentive.com", desigManager);
        UserEntity finHead = ensureUser("finance_head", "Finance Head (Farhan)", "farhan@incentive.com", desigFinance);
        UserEntity acctsHead = ensureUser("accounts_head", "Accounts Head (Anita)", "anita@incentive.com", desigFinance);
        UserEntity dirPriya = ensureUser("director_priya", "Director Priya", "priya@incentive.com", desigDirector);
        UserEntity bizHead = ensureUser("biz_head", "Business Head (Vikram)", "vikram@incentive.com", desigDirector);

        // 4. Grant Permissions on PARTY_PAYMENT
        // Executive can view, create, edit, and submit
        grantPermissions(desigExecutive, page, true, true, true, false, true, true, false, false);
        // Managers can view, approve, reject
        grantPermissions(desigManager, page, true, false, false, false, true, false, true, true);
        // Finance can view, approve, reject
        grantPermissions(desigFinance, page, true, false, false, false, true, false, true, true);
        // Directors can view, approve, reject
        grantPermissions(desigDirector, page, true, false, false, false, true, false, true, true);

        // 5. Create or verify PARTY_PAYMENT_APPROVAL workflow
        Optional<ApprovalWorkflowEntity> existingWf = workflowRepository.findByEntityTypeAndIsActiveTrue(WorkflowEntityType.PARTY_PAYMENT);
        if (existingWf.isEmpty()) {
            ApprovalWorkflowEntity workflow = new ApprovalWorkflowEntity();
            workflow.setWorkflowName("PARTY_PAYMENT_APPROVAL");
            workflow.setEntityType(WorkflowEntityType.PARTY_PAYMENT);
            workflow.setDescription("3-Level Sequential Approval Workflow for Party Payments");
            workflow.setIsActive(true);
            workflow.setRequireMakerChecker(true);
            workflow = workflowRepository.save(workflow);

            // Level 1: Manager Approval (Manager A, Manager B)
            ApprovalWorkflowLevelEntity l1 = new ApprovalWorkflowLevelEntity();
            l1.setWorkflow(workflow);
            l1.setLevelNumber(1);
            l1.setLevelName("Manager Approval");
            l1.setSequenceOrder(1);
            l1.setIsActive(true);
            l1 = levelRepository.save(l1);
            addApprover(l1, mgrA);
            addApprover(l1, mgrB);

            // Level 2: Finance Approval (Finance Head, Accounts Head)
            ApprovalWorkflowLevelEntity l2 = new ApprovalWorkflowLevelEntity();
            l2.setWorkflow(workflow);
            l2.setLevelNumber(2);
            l2.setLevelName("Finance Approval");
            l2.setSequenceOrder(2);
            l2.setIsActive(true);
            l2 = levelRepository.save(l2);
            addApprover(l2, finHead);
            addApprover(l2, acctsHead);

            // Level 3: Business Approval (Business Head, Director Priya)
            ApprovalWorkflowLevelEntity l3 = new ApprovalWorkflowLevelEntity();
            l3.setWorkflow(workflow);
            l3.setLevelNumber(3);
            l3.setLevelName("Business Approval");
            l3.setSequenceOrder(3);
            l3.setIsActive(true);
            l3 = levelRepository.save(l3);
            addApprover(l3, bizHead);
            addApprover(l3, dirPriya);

            log.info("Initialized 3-level PARTY_PAYMENT_APPROVAL workflow successfully.");
        }
    }

    private PageEntity ensurePage(String code, String name, String module, String route, String icon, int order) {
        return pageRepository.findByPageCode(code).orElseGet(() -> {
            PageEntity p = PageEntity.builder()
                    .pageCode(code)
                    .pageName(name)
                    .moduleName(module)
                    .route(route)
                    .icon(icon)
                    .displayOrder(order)
                    .isActive(true)
                    .build();
            return pageRepository.save(p);
        });
    }

    private DesignationEntity ensureDesignation(String code, String name, String level) {
        return designationRepository.findByDesignationCode(code).orElseGet(() -> {
            DesignationEntity d = DesignationEntity.builder()
                    .designationCode(code)
                    .designationName(name)
                    .level(level)
                    .description(name)
                    .isActive(true)
                    .build();
            return designationRepository.save(d);
        });
    }

    private UserEntity ensureUser(String username, String fullName, String email, DesignationEntity designation) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity u = UserEntity.builder()
                    .username(username)
                    .password(passwordEncoder.encode("Test@123"))
                    .fullName(fullName)
                    .email(email)
                    .designation(designation)
                    .status(UserStatus.ACTIVE)
                    .role("USER")
                    .build();
            return userRepository.save(u);
        });
    }

    private void grantPermissions(DesignationEntity designation, PageEntity page,
                                  boolean view, boolean create, boolean edit, boolean delete,
                                  boolean export, boolean submit, boolean approve, boolean reject) {
        Optional<DesignationPagePermissionEntity> opt = permissionRepository.findByDesignationIdAndPageId(
                designation.getId(), page.getId());
        DesignationPagePermissionEntity perm = opt.orElseGet(() -> DesignationPagePermissionEntity.builder()
                .designation(designation)
                .page(page)
                .build());

        perm.setCanView(view);
        perm.setCanCreate(create);
        perm.setCanEdit(edit);
        perm.setCanDelete(delete);
        perm.setCanExport(export);
        perm.setCanSubmit(submit);
        perm.setCanApprove(approve);
        perm.setCanReject(reject);
        permissionRepository.save(perm);
    }

    private void addApprover(ApprovalWorkflowLevelEntity level, UserEntity user) {
        ApprovalWorkflowApproverEntity app = new ApprovalWorkflowApproverEntity();
        app.setWorkflowLevel(level);
        app.setUser(user);
        app.setIsActive(true);
        approverRepository.save(app);
    }
}
