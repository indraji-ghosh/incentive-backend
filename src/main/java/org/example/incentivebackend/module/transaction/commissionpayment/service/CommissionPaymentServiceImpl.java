package org.example.incentivebackend.module.transaction.commissionpayment.service;

import jakarta.annotation.PostConstruct;
import org.example.incentivebackend.common.audit.enums.AuditAction;
import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.audit.util.AuditHelper;
import org.example.incentivebackend.common.enums.PaymentStatusEnum;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.approval.dto.ApprovalDetailsDTO;
import org.example.incentivebackend.module.approval.enums.ApprovalStatus;
import org.example.incentivebackend.module.approval.enums.WorkflowEntityType;
import org.example.incentivebackend.module.approval.service.ApprovalService;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.master.user.UserEntity;
import org.example.incentivebackend.module.master.user.UserRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentFilter;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.PartyCommissionPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.PartyPaymentAdjustmentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.mapper.CommissionPaymentMapper;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.PartyPaymentAdjustmentRepository;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommissionPaymentServiceImpl implements CommissionPaymentService {

    private final CommissionPaymentRepository repository;
    private final PartyPaymentAdjustmentRepository adjustmentRepository;
    private final PartyRepository partyRepository;
    private final CommissionCalculationService commissionCalculationService;
    private final CommissionPaymentMapper mapper;
    private final JdbcTemplate jdbcTemplate;
    private final PartyPayableRepository partyPayableRepository;
    private final AuditLogService auditLogService;
    private final AuditHelper auditHelper;
    private final ApprovalService approvalService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public CommissionPaymentServiceImpl(
            CommissionPaymentRepository repository,
            PartyPaymentAdjustmentRepository adjustmentRepository,
            PartyRepository partyRepository,
            CommissionCalculationService commissionCalculationService,
            CommissionPaymentMapper mapper,
            JdbcTemplate jdbcTemplate,
            PartyPayableRepository partyPayableRepository,
            AuditLogService auditLogService,
            AuditHelper auditHelper,
            ApprovalService approvalService,
            CurrentUserService currentUserService,
            UserRepository userRepository) {
        this.repository = repository;
        this.adjustmentRepository = adjustmentRepository;
        this.partyRepository = partyRepository;
        this.commissionCalculationService = commissionCalculationService;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
        this.partyPayableRepository = partyPayableRepository;
        this.auditLogService = auditLogService;
        this.auditHelper = auditHelper;
        this.approvalService = approvalService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void fixStaleForeignKey() {
        try {
            jdbcTemplate.execute("ALTER TABLE tr_commission_payment DROP CONSTRAINT FKFSRN80B5P08P0YYI6LRB6EYIK");
        } catch (Exception ignored) {}
    }

    @Override
    @Transactional
    public CommissionPaymentResponse create(CommissionPaymentRequest request) {
        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));

        BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(party.getId());
        BigDecimal totalPaid = getPayablePaidAndAdjusted(party.getId());
        BigDecimal outstanding = totalEarned.subtract(totalPaid);

        if (request.getPaymentAmount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Commission payment amount exceeds outstanding commission");
        }
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No outstanding commission left for this party");
        }

        if (request.getAllocations() == null || request.getAllocations().isEmpty()) {
            throw new IllegalArgumentException("At least one payable allocation must exist for a payable-related payment");
        }

        BigDecimal totalAllocated = BigDecimal.ZERO;
        for (CommissionPaymentRequest.PaymentAllocationRequest alloc : request.getAllocations()) {
            if (alloc.getAmount() == null || alloc.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Allocation amount must be greater than zero");
            }
            totalAllocated = totalAllocated.add(alloc.getAmount());
        }

        if (totalAllocated.compareTo(request.getPaymentAmount()) != 0) {
            throw new IllegalArgumentException("Total allocation amount must equal payment amount");
        }

        // RULE 6: Creation must be DRAFT status!
        CommissionPaymentEntity entity = new CommissionPaymentEntity();
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        entity.setStatus("DRAFT");
        entity.setPaymentType("PAYABLE_PAYMENT");
        entity.setPaymentNo("PP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        entity = repository.save(entity);

        // Save allocations/annexure without mutating payables yet (payables remain unchanged until PAID status)
        for (CommissionPaymentRequest.PaymentAllocationRequest alloc : request.getAllocations()) {
            PartyPayableEntity payable = partyPayableRepository.findById(alloc.getPayableId())
                    .orElseThrow(() -> new IllegalArgumentException("Payable not found: " + alloc.getPayableId()));

            if (!payable.getParty().getId().equals(party.getId())) {
                throw new IllegalArgumentException("Allocation payable must belong to the selected party");
            }

            if (!StatusEnum.A.equals(payable.getStatus())) {
                throw new IllegalArgumentException("Deleted/inactive payable cannot receive payment");
            }

            BigDecimal currentPaid = payable.getPaidAmount() != null ? payable.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal payableOutstanding = payable.getOutstandingAmount() != null ? payable.getOutstandingAmount() : payable.getPayableAmount().subtract(currentPaid);

            if (alloc.getAmount().compareTo(payableOutstanding) > 0) {
                throw new IllegalArgumentException("Allocation amount cannot exceed payable outstanding amount");
            }

            PartyPaymentAdjustmentEntity adjustment = new PartyPaymentAdjustmentEntity();
            adjustment.setCommissionPayment(entity);
            adjustment.setPayable(payable);
            adjustment.setAdjustedAmount(alloc.getAmount());
            adjustment.setAllocatedAmount(alloc.getAmount());
            adjustmentRepository.save(adjustment);
        }

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);

        Long currentUserId = currentUserService.getCurrentUser().map(UserEntity::getUserId).orElse(1L);
        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.CREATE,
                null, auditHelper.toJson(response),
                "Party Payment created as DRAFT", currentUserId,
                entity.getPaymentNo(), "Party", "ms_party", party.getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse createAdvancePayment(CommissionPaymentRequest request) {
        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));

        if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advance payment amount must be greater than zero");
        }

        CommissionPaymentEntity entity = new CommissionPaymentEntity();
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        entity.setStatus("DRAFT");
        entity.setPaymentType("ADVANCE_PAYMENT");
        entity.setAdjustedAmount(BigDecimal.ZERO);
        entity.setPaymentNo("ADV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        entity = repository.save(entity);
        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);

        Long currentUserId = currentUserService.getCurrentUser().map(UserEntity::getUserId).orElse(1L);
        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.CREATE,
                null, auditHelper.toJson(response),
                "Party Advance Payment created as DRAFT", currentUserId,
                entity.getPaymentNo(), "Party", "ms_party", party.getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse submit(Long id, String remarks) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party payment not found: " + id));

        if (!"DRAFT".equals(entity.getStatus()) && !"REJECTED".equals(entity.getStatus())) {
            throw new IllegalStateException("Payment can only be submitted from DRAFT or REJECTED status. Current status: " + entity.getStatus());
        }

        UserEntity currentUser = currentUserService.getCurrentUserOrThrow();

        // Start or resubmit approval workflow
        ApprovalDetailsDTO approval = approvalService.submit(
                WorkflowEntityType.PARTY_PAYMENT, id, currentUser, remarks);

        entity.setStatus("PENDING_APPROVAL");
        entity.setSubmittedBy(currentUser.getUserId());
        entity.setSubmittedAt(LocalDateTime.now());
        entity = repository.save(entity);

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);
        response.setApproval(approval);

        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.SUBMIT,
                null, auditHelper.toJson(response),
                "Party Payment submitted for multi-level approval", currentUser.getUserId(),
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty().getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse approve(Long id, String remarks) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party payment not found: " + id));

        if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
            throw new IllegalStateException("Payment is not pending approval. Current status: " + entity.getStatus());
        }

        UserEntity currentUser = currentUserService.getCurrentUserOrThrow();

        ApprovalDetailsDTO approval = approvalService.approve(
                WorkflowEntityType.PARTY_PAYMENT, id, currentUser, remarks);

        if (ApprovalStatus.APPROVED.equals(approval.getStatus())) {
            entity.setStatus("APPROVED");
            entity.setApprovedAt(LocalDateTime.now());
            entity = repository.save(entity);
        }

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);
        response.setApproval(approval);

        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.APPROVE,
                null, auditHelper.toJson(response),
                "Party Payment approval step processed (Status: " + entity.getStatus() + ")", currentUser.getUserId(),
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty().getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse reject(Long id, String reason) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party payment not found: " + id));

        if (!"PENDING_APPROVAL".equals(entity.getStatus())) {
            throw new IllegalStateException("Payment is not pending approval. Current status: " + entity.getStatus());
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is mandatory");
        }

        UserEntity currentUser = currentUserService.getCurrentUserOrThrow();

        ApprovalDetailsDTO approval = approvalService.reject(
                WorkflowEntityType.PARTY_PAYMENT, id, currentUser, reason);

        entity.setStatus("REJECTED");
        entity.setRejectedBy(currentUser.getUserId());
        entity.setRejectedAt(LocalDateTime.now());
        entity.setRejectionReason(reason);
        entity = repository.save(entity);

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);
        response.setApproval(approval);

        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.REJECT,
                null, auditHelper.toJson(response),
                "Party Payment rejected: " + reason, currentUser.getUserId(),
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty().getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse pay(Long id) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party payment not found: " + id));

        if (!"APPROVED".equals(entity.getStatus())) {
            throw new IllegalStateException("Payment must be in APPROVED status before processing disbursement. Current status: " + entity.getStatus());
        }

        UserEntity currentUser = currentUserService.getCurrentUserOrThrow();

        // Financial posting: Apply deductions to accrued payables now that disbursement is executed
        if ("PAYABLE_PAYMENT".equals(entity.getPaymentType())) {
            List<PartyPaymentAdjustmentEntity> adjustments = adjustmentRepository
                    .findByCommissionPayment_CommissionPaymentId(entity.getCommissionPaymentId());

            for (PartyPaymentAdjustmentEntity adj : adjustments) {
                PartyPayableEntity payable = partyPayableRepository.findById(adj.getPayable().getId())
                        .orElseThrow(() -> new IllegalStateException("Payable not found: " + adj.getPayable().getId()));

                BigDecimal currentPaid = payable.getPaidAmount() != null ? payable.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal newPaid = currentPaid.add(adj.getAdjustedAmount());
                BigDecimal newOutstanding = payable.getPayableAmount().subtract(newPaid);

                payable.setPaidAmount(newPaid);
                payable.setOutstandingAmount(newOutstanding);

                if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
                    payable.setPaymentStatus(PaymentStatusEnum.PAID);
                } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                    payable.setPaymentStatus(PaymentStatusEnum.PARTIALLY_PAID);
                }

                partyPayableRepository.save(payable);
            }
        }

        entity.setStatus("PAID");
        entity = repository.save(entity);

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);

        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.UPDATE,
                null, auditHelper.toJson(response),
                "Party Payment marked as PAID and financial posting executed", currentUser.getUserId(),
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty().getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse cancel(Long id, String reason) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party payment not found: " + id));

        if ("PAID".equals(entity.getStatus())) {
            throw new IllegalStateException("Cannot cancel an already paid payment voucher");
        }

        UserEntity currentUser = currentUserService.getCurrentUserOrThrow();

        if ("PENDING_APPROVAL".equals(entity.getStatus())) {
            approvalService.cancel(WorkflowEntityType.PARTY_PAYMENT, id, currentUser, reason);
        }

        entity.setStatus("CANCELLED");
        entity = repository.save(entity);

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);

        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.CANCEL,
                null, auditHelper.toJson(response),
                "Party Payment cancelled: " + reason, currentUser.getUserId(),
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty().getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    public ApprovalDetailsDTO getApproval(Long id) {
        UserEntity currentUser = currentUserService.getCurrentUser().orElse(null);
        return approvalService.getApprovalDetails(WorkflowEntityType.PARTY_PAYMENT, id, currentUser);
    }

    @Override
    public Page<CommissionPaymentResponse> getVouchers(int page, int size, String status, Long partyId, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommissionPaymentEntity> entities = repository.findAllByOrderByPaymentDateDescCommissionPaymentIdDesc(pageable);

        List<CommissionPaymentResponse> responses = entities.getContent().stream()
                .filter(p -> status == null || status.isBlank() || "all".equalsIgnoreCase(status) || p.getStatus().equalsIgnoreCase(status))
                .filter(p -> partyId == null || p.getParty().getId().equals(partyId))
                .filter(p -> search == null || search.isBlank() ||
                        p.getPaymentNo().toLowerCase().contains(search.toLowerCase()) ||
                        p.getParty().getPartyName().toLowerCase().contains(search.toLowerCase()))
                .map(p -> enrichResponseWithAllocations(mapper.toResponse(p), p))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, entities.getTotalElements());
    }

    @Override
    @Transactional
    public CommissionPaymentResponse adjustAdvance(Long partyId, Long payableId, BigDecimal adjustmentAmount) {
        PartyPayableEntity payable = partyPayableRepository.findById(payableId)
                .orElseThrow(() -> new ResourceNotFoundException("Payable not found: " + payableId));

        if (!partyId.equals(payable.getParty().getId())) {
            throw new IllegalArgumentException("Payable must belong to the party");
        }

        if (adjustmentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Adjustment amount must be greater than zero");
        }

        BigDecimal availableAdvance = getAvailableAdvanceBalance(partyId);
        if (adjustmentAmount.compareTo(availableAdvance) > 0) {
            throw new IllegalArgumentException("Adjustment exceeds available advance");
        }

        List<CommissionPaymentEntity> advances = repository.findAvailableAdvancesForParty(partyId);

        BigDecimal remainingToAdjust = adjustmentAmount;
        CommissionPaymentEntity lastAdvance = null;

        for (CommissionPaymentEntity advance : advances) {
            if (remainingToAdjust.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal advanceAvailable = advance.getPaymentAmount().subtract(advance.getAdjustedAmount());
            BigDecimal amountToTake = remainingToAdjust.min(advanceAvailable);

            PartyPaymentAdjustmentEntity adjustment = new PartyPaymentAdjustmentEntity();
            adjustment.setCommissionPayment(advance);
            adjustment.setPayable(payable);
            adjustment.setAdjustedAmount(amountToTake);
            adjustment.setAllocatedAmount(amountToTake);
            adjustmentRepository.save(adjustment);

            advance.setAdjustedAmount(advance.getAdjustedAmount().add(amountToTake));
            lastAdvance = repository.save(advance);

            remainingToAdjust = remainingToAdjust.subtract(amountToTake);
        }

        if (lastAdvance == null) {
            throw new IllegalStateException("No advances available to adjust");
        }
        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(lastAdvance), lastAdvance);

        Long currentUserId = currentUserService.getCurrentUser().map(UserEntity::getUserId).orElse(1L);
        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", lastAdvance.getCommissionPaymentId(),
                AuditAction.UPDATE,
                null, auditHelper.toJson(response),
                "Party Advance Payment adjusted", currentUserId,
                lastAdvance.getPaymentNo(), "PartyPayable", "td_party_payable", payable.getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    public BigDecimal getAvailableAdvanceBalance(Long partyId) {
        BigDecimal totalAdvance = repository.sumActiveAdvanceAmountByPartyId(partyId);
        BigDecimal totalAdjusted = repository.sumAdjustedAdvanceAmountByPartyId(partyId);
        if (totalAdvance == null) totalAdvance = BigDecimal.ZERO;
        if (totalAdjusted == null) totalAdjusted = BigDecimal.ZERO;
        return totalAdvance.subtract(totalAdjusted);
    }

    private BigDecimal getPayablePaidAndAdjusted(Long partyId) {
        BigDecimal totalPayablePaid = repository.sumActivePayablePaymentAmountByPartyId(partyId);
        BigDecimal totalAdvanceAdjusted = repository.sumAdjustedAdvanceAmountByPartyId(partyId);
        if (totalPayablePaid == null) totalPayablePaid = BigDecimal.ZERO;
        if (totalAdvanceAdjusted == null) totalAdvanceAdjusted = BigDecimal.ZERO;
        return totalPayablePaid.add(totalAdvanceAdjusted);
    }

    @Override
    public Page<CommissionPaymentSummaryResponse> getSummary(CommissionPaymentFilter filter) {
        Page<PartyEntity> parties;
        if (filter != null && filter.getPartyId() != null) {
            Optional<PartyEntity> p = partyRepository.findById(filter.getPartyId());
            parties = p.map(partyEntity -> new PageImpl<>(List.of(partyEntity), PageRequest.of(0, 1), 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));
        } else if (filter != null && filter.getPartyName() != null && !filter.getPartyName().isBlank()) {
            parties = partyRepository.findAll((root, query, cb) ->
                            cb.like(cb.lower(root.get("partyName")), "%" + filter.getPartyName().toLowerCase() + "%"),
                    PageRequest.of(filter != null ? filter.getPage() : 0, filter != null ? filter.getSize() : 10));
        } else {
            int page = filter != null ? filter.getPage() : 0;
            int size = filter != null ? filter.getSize() : 10;
            parties = partyRepository.findAll(PageRequest.of(page, size));
        }

        List<CommissionPaymentSummaryResponse> summaries = parties.getContent().stream().map(party -> {
            CommissionPaymentSummaryResponse res = new CommissionPaymentSummaryResponse();
            res.setPartyId(party.getId());
            res.setPartyName(party.getPartyName());

            BigDecimal earned = commissionCalculationService.getTotalCommissionEarned(party.getId());
            BigDecimal paid = getPayablePaidAndAdjusted(party.getId());
            BigDecimal outstanding = earned.subtract(paid);

            res.setTotalCommissionEarned(earned);
            res.setTotalCommissionPaid(paid);
            res.setOutstandingCommission(outstanding);

            BigDecimal totalAdvance = repository.sumActiveAdvanceAmountByPartyId(party.getId());
            BigDecimal totalAdvanceAdjusted = repository.sumAdjustedAdvanceAmountByPartyId(party.getId());

            if (totalAdvance == null) totalAdvance = BigDecimal.ZERO;
            if (totalAdvanceAdjusted == null) totalAdvanceAdjusted = BigDecimal.ZERO;

            res.setTotalAdvance(totalAdvance);
            res.setTotalAdvanceAdjusted(totalAdvanceAdjusted);
            res.setAvailableAdvance(totalAdvance.subtract(totalAdvanceAdjusted));

            if (paid.compareTo(BigDecimal.ZERO) == 0) {
                res.setPaymentStatus("UNPAID");
            } else if (paid.compareTo(earned) >= 0) {
                res.setPaymentStatus("PAID");
            } else {
                res.setPaymentStatus("PARTIALLY_PAID");
            }

            return res;
        }).collect(Collectors.toList());

        return new PageImpl<>(summaries, parties.getPageable(), parties.getTotalElements());
    }

    @Override
    public CommissionPaymentResponse getById(Long id) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        return enrichResponseWithAllocations(mapper.toResponse(entity), entity);
    }

    @Override
    public PartyCommissionPaymentHistoryResponse getPartyHistory(Long partyId) {
        PartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + partyId));

        List<CommissionPaymentEntity> entities = repository
                .findByParty_IdOrderByPaymentDateDescCommissionPaymentIdDesc(partyId);

        List<CommissionPaymentResponse> payments = entities.stream()
                .filter(p -> !"DELETED".equals(p.getStatus()))
                .map(entity -> enrichResponseWithAllocations(mapper.toResponse(entity), entity))
                .collect(Collectors.toList());

        BigDecimal earned = commissionCalculationService.getTotalCommissionEarned(partyId);
        BigDecimal paid = getPayablePaidAndAdjusted(partyId);
        BigDecimal outstanding = earned.subtract(paid);

        String paymentStatus;
        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = "UNPAID";
        } else if (paid.compareTo(earned) >= 0) {
            paymentStatus = "PAID";
        } else {
            paymentStatus = "PARTIALLY_PAID";
        }

        PartyCommissionPaymentHistoryResponse res = new PartyCommissionPaymentHistoryResponse();
        res.setPartyId(partyId);
        res.setPartyName(party.getPartyName());
        res.setTotalCommissionEarned(earned);
        res.setTotalCommissionPaid(paid);
        res.setOutstandingCommission(outstanding);
        res.setPaymentStatus(paymentStatus);
        res.setPayments(payments);

        BigDecimal totalAdvance = repository.sumActiveAdvanceAmountByPartyId(partyId);
        BigDecimal totalAdvanceAdjusted = repository.sumAdjustedAdvanceAmountByPartyId(partyId);

        if (totalAdvance == null) totalAdvance = BigDecimal.ZERO;
        if (totalAdvanceAdjusted == null) totalAdvanceAdjusted = BigDecimal.ZERO;

        res.setTotalAdvance(totalAdvance);
        res.setTotalAdvanceAdjusted(totalAdvanceAdjusted);
        res.setAvailableAdvance(totalAdvance.subtract(totalAdvanceAdjusted));

        return res;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse update(Long id, CommissionPaymentRequest request) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!"DRAFT".equals(entity.getStatus()) && !"REJECTED".equals(entity.getStatus())) {
            throw new IllegalStateException("Cannot edit payment in " + entity.getStatus() + " status. Only DRAFT or REJECTED payments can be modified.");
        }

        String oldStateJson = auditHelper.toJson(enrichResponseWithAllocations(mapper.toResponse(entity), entity));

        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));

        if (entity.getPaymentType() != null && entity.getPaymentType().equals("ADVANCE_PAYMENT")) {
            if (request.getPaymentAmount().compareTo(entity.getAdjustedAmount()) < 0) {
                throw new IllegalArgumentException("Advance payment amount cannot be less than the already adjusted amount");
            }
        } else {
            BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(party.getId());
            BigDecimal paidExcludingCurrent = repository.sumActivePayablePaymentAmountByPartyIdExcluding(party.getId(), id)
                    .add(repository.sumAdjustedAdvanceAmountByPartyId(party.getId()));

            BigDecimal maxAllowed = totalEarned.subtract(paidExcludingCurrent);

            if (request.getPaymentAmount().compareTo(maxAllowed) > 0) {
                throw new IllegalArgumentException("Updated amount exceeds total outstanding commission");
            }
        }

        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());

        // If previously REJECTED, update resets to DRAFT for clean resubmission
        if ("REJECTED".equals(entity.getStatus())) {
            entity.setStatus("DRAFT");
        }

        entity = repository.save(entity);

        // Update allocations if provided
        if (request.getAllocations() != null && !request.getAllocations().isEmpty()) {
            adjustmentRepository.deleteAll(adjustmentRepository.findByCommissionPayment_CommissionPaymentId(entity.getCommissionPaymentId()));
            for (CommissionPaymentRequest.PaymentAllocationRequest alloc : request.getAllocations()) {
                PartyPayableEntity payable = partyPayableRepository.findById(alloc.getPayableId())
                        .orElseThrow(() -> new IllegalArgumentException("Payable not found: " + alloc.getPayableId()));

                PartyPaymentAdjustmentEntity adjustment = new PartyPaymentAdjustmentEntity();
                adjustment.setCommissionPayment(entity);
                adjustment.setPayable(payable);
                adjustment.setAdjustedAmount(alloc.getAmount());
                adjustment.setAllocatedAmount(alloc.getAmount());
                adjustmentRepository.save(adjustment);
            }
        }

        CommissionPaymentResponse response = enrichResponseWithAllocations(mapper.toResponse(entity), entity);

        Long currentUserId = currentUserService.getCurrentUser().map(UserEntity::getUserId).orElse(1L);
        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", entity.getCommissionPaymentId(),
                AuditAction.UPDATE,
                oldStateJson, auditHelper.toJson(response),
                "Party Payment updated", currentUserId,
                entity.getPaymentNo(), "Party", "ms_party", party.getId(), "SUCCESS"
        );

        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if ("PAID".equals(entity.getStatus())) {
            throw new IllegalStateException("Cannot delete an already PAID payment voucher");
        }
        if ("PENDING_APPROVAL".equals(entity.getStatus())) {
            throw new IllegalStateException("Cannot delete a payment that is currently PENDING_APPROVAL. Cancel it first.");
        }

        String oldStateJson = auditHelper.toJson(enrichResponseWithAllocations(mapper.toResponse(entity), entity));

        entity.setStatus("DELETED");
        repository.save(entity);

        Long currentUserId = currentUserService.getCurrentUser().map(UserEntity::getUserId).orElse(1L);
        auditLogService.createAuditLog(
                "TRANSACTION", "CommissionPayment", "tr_commission_payment", id,
                AuditAction.DELETE,
                oldStateJson, null,
                "Party Payment deleted", currentUserId,
                entity.getPaymentNo(), "Party", "ms_party", entity.getParty() != null ? entity.getParty().getId() : null, "SUCCESS"
        );
    }

    private CommissionPaymentResponse enrichResponseWithAllocations(CommissionPaymentResponse response, CommissionPaymentEntity entity) {
        List<PartyPaymentAdjustmentEntity> adjustments = adjustmentRepository.findByCommissionPayment_CommissionPaymentId(entity.getCommissionPaymentId());
        if (adjustments != null && !adjustments.isEmpty()) {
            List<CommissionPaymentResponse.PaymentAllocationResponse> allocations = adjustments.stream().map(adj -> {
                CommissionPaymentResponse.PaymentAllocationResponse alloc = new CommissionPaymentResponse.PaymentAllocationResponse();
                alloc.setAccruedPayableId(adj.getPayable().getId());
                alloc.setPayableType(adj.getPayable().getSourceType());
                alloc.setReference(adj.getPayable().getSourceReference() != null ? adj.getPayable().getSourceReference() : adj.getPayable().getSourceId());
                alloc.setOriginalAmount(adj.getPayable().getPayableAmount());
                alloc.setPreviouslyPaid(adj.getPayable().getPaidAmount() != null ? adj.getPayable().getPaidAmount().subtract(adj.getAdjustedAmount()) : BigDecimal.ZERO);
                alloc.setAllocatedAmount(adj.getAdjustedAmount());
                alloc.setRemainingAmount(adj.getPayable().getOutstandingAmount());
                return alloc;
            }).collect(Collectors.toList());
            response.setAllocations(allocations);
        }

        // Submitter & Rejecter names
        if (entity.getSubmittedBy() != null) {
            response.setSubmittedBy(entity.getSubmittedBy());
            userRepository.findById(entity.getSubmittedBy())
                    .ifPresent(u -> response.setSubmittedByName(u.getFullName()));
        }
        if (entity.getRejectedBy() != null) {
            response.setRejectedBy(entity.getRejectedBy());
            userRepository.findById(entity.getRejectedBy())
                    .ifPresent(u -> response.setRejectedByName(u.getFullName()));
        }
        response.setSubmittedAt(entity.getSubmittedAt());
        response.setApprovedAt(entity.getApprovedAt());
        response.setRejectedAt(entity.getRejectedAt());
        response.setRejectionReason(entity.getRejectionReason());

        // Approval details
        UserEntity currentUser = currentUserService.getCurrentUser().orElse(null);
        ApprovalDetailsDTO approval = approvalService.getApprovalDetails(
                WorkflowEntityType.PARTY_PAYMENT, entity.getCommissionPaymentId(), currentUser);
        response.setApproval(approval);

        return response;
    }
}
