package org.example.incentivebackend.module.transaction.commissionpayment.service;

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
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.common.enums.PaymentStatusEnum;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.incentivebackend.common.enums.StatusEnum;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;
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

    public CommissionPaymentServiceImpl(
            CommissionPaymentRepository repository,
            PartyPaymentAdjustmentRepository adjustmentRepository,
            PartyRepository partyRepository,
            CommissionCalculationService commissionCalculationService,
            CommissionPaymentMapper mapper,
            JdbcTemplate jdbcTemplate,
            PartyPayableRepository partyPayableRepository) {
        this.repository = repository;
        this.adjustmentRepository = adjustmentRepository;
        this.partyRepository = partyRepository;
        this.commissionCalculationService = commissionCalculationService;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
        this.partyPayableRepository = partyPayableRepository;
    }

    @PostConstruct
    public void fixStaleForeignKey() {
        try {
            jdbcTemplate.execute("ALTER TABLE tr_commission_payment DROP CONSTRAINT FKFSRN80B5P08P0YYI6LRB6EYIK");
        } catch (Exception e) {}
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

        CommissionPaymentEntity entity = new CommissionPaymentEntity();
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        entity.setStatus("ACTIVE");
        entity.setPaymentType("PAYABLE_PAYMENT");
        entity.setPaymentNo("CP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        entity = repository.save(entity);

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

            BigDecimal newPaid = currentPaid.add(alloc.getAmount());
            BigDecimal newOutstanding = payable.getPayableAmount().subtract(newPaid);

            payable.setPaidAmount(newPaid);
            payable.setOutstandingAmount(newOutstanding);

            if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
                payable.setPaymentStatus(PaymentStatusEnum.PAID);
            } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
                payable.setPaymentStatus(PaymentStatusEnum.PARTIALLY_PAID);
            }

            partyPayableRepository.save(payable);

            PartyPaymentAdjustmentEntity adjustment = new PartyPaymentAdjustmentEntity();
            adjustment.setCommissionPayment(entity);
            adjustment.setPayable(payable);
            adjustment.setAdjustedAmount(alloc.getAmount());
            adjustment.setAllocatedAmount(alloc.getAmount());
            adjustmentRepository.save(adjustment);
        }

        return enrichResponseWithAllocations(mapper.toResponse(entity), entity);
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
        entity.setStatus("ACTIVE");
        entity.setPaymentType("ADVANCE_PAYMENT");
        entity.setAdjustedAmount(BigDecimal.ZERO);
        entity.setPaymentNo("ADV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        entity = repository.save(entity);
        return enrichResponseWithAllocations(mapper.toResponse(entity), entity);
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

        List<CommissionPaymentEntity> advances = repository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE").stream()
                .filter(p -> "ADVANCE_PAYMENT".equals(p.getPaymentType()))
                .filter(p -> p.getPaymentAmount().subtract(p.getAdjustedAmount()).compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(CommissionPaymentEntity::getPaymentDate))
                .toList();

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
        return enrichResponseWithAllocations(mapper.toResponse(lastAdvance), lastAdvance);
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
                
        List<CommissionPaymentEntity> entities = repository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE");
        List<CommissionPaymentResponse> payments = entities.stream()
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
                
        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));
                
        if (entity.getPaymentType() != null && entity.getPaymentType().equals("ADVANCE_PAYMENT")) {
            // Cannot update advance payment amount if it becomes less than what is already adjusted
            if (request.getPaymentAmount().compareTo(entity.getAdjustedAmount()) < 0) {
                throw new IllegalArgumentException("Advance payment amount cannot be less than the already adjusted amount");
            }
        } else {
            BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(party.getId());
            BigDecimal paidExcludingCurrent = repository.sumActivePayablePaymentAmountByPartyIdExcluding(party.getId(), id).add(repository.sumAdjustedAdvanceAmountByPartyId(party.getId()));
            
            BigDecimal maxAllowed = totalEarned.subtract(paidExcludingCurrent);
            
            if (request.getPaymentAmount().compareTo(maxAllowed) > 0) {
                throw new IllegalArgumentException("Updated amount exceeds total outstanding commission");
            }
        }
        
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        
        entity = repository.save(entity);
        return enrichResponseWithAllocations(mapper.toResponse(entity), entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
                
        if (entity.getPaymentType() != null && entity.getPaymentType().equals("ADVANCE_PAYMENT")) {
            if (entity.getAdjustedAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalArgumentException("Cannot delete an advance payment that has already been adjusted");
            }
        }
        // Soft delete
        entity.setStatus("DELETED");
        repository.save(entity);
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
        return response;
    }
}
