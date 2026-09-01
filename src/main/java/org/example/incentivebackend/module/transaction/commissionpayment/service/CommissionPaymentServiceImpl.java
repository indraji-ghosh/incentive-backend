package org.example.incentivebackend.module.transaction.commissionpayment.service;

import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentFilter;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.request.CommissionPaymentRequest;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.CommissionPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.dto.response.PartyCommissionPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.commissionpayment.entity.CommissionPaymentEntity;
import org.example.incentivebackend.module.transaction.commissionpayment.mapper.CommissionPaymentMapper;
import org.example.incentivebackend.module.transaction.commissionpayment.repository.CommissionPaymentRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommissionPaymentServiceImpl implements CommissionPaymentService {

    private final CommissionPaymentRepository repository;
    private final PartyEntryRepository partyEntryRepository;
    private final CommissionCalculationService commissionCalculationService;
    private final CommissionPaymentMapper mapper;

    public CommissionPaymentServiceImpl(
            CommissionPaymentRepository repository,
            PartyEntryRepository partyEntryRepository,
            CommissionCalculationService commissionCalculationService,
            CommissionPaymentMapper mapper) {
        this.repository = repository;
        this.partyEntryRepository = partyEntryRepository;
        this.commissionCalculationService = commissionCalculationService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse create(CommissionPaymentRequest request) {
        PartyEntryEntity party = partyEntryRepository.findById(request.getPartyId())
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));

        BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(party.getId());
        BigDecimal totalPaid = repository.sumActivePaymentAmountByPartyId(party.getId());
        BigDecimal outstanding = totalEarned.subtract(totalPaid);

        if (request.getPaymentAmount().compareTo(outstanding) > 0) {
            throw new IllegalArgumentException("Commission payment amount exceeds outstanding commission");
        }
        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No outstanding commission left for this party");
        }

        CommissionPaymentEntity entity = new CommissionPaymentEntity();
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        entity.setStatus("ACTIVE");
        
        // Auto-generate unique paymentNo
        entity.setPaymentNo("CP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public Page<CommissionPaymentSummaryResponse> getSummary(CommissionPaymentFilter filter) {
        // Find all parties matching filter
        Page<PartyEntryEntity> parties = partyEntryRepository.findAll(PageRequest.of(filter.getPage(), filter.getSize()));
        
        List<CommissionPaymentSummaryResponse> summaries = parties.getContent().stream().map(party -> {
            CommissionPaymentSummaryResponse res = new CommissionPaymentSummaryResponse();
            res.setPartyId(party.getId());
            res.setPartyName(party.getPartyName());
            
            BigDecimal earned = commissionCalculationService.getTotalCommissionEarned(party.getId());
            BigDecimal paid = repository.sumActivePaymentAmountByPartyId(party.getId());
            BigDecimal outstanding = earned.subtract(paid);
            
            res.setTotalCommissionEarned(earned);
            res.setTotalCommissionPaid(paid);
            res.setOutstandingCommission(outstanding);
            
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
        return mapper.toResponse(entity);
    }

    @Override
    public PartyCommissionPaymentHistoryResponse getPartyHistory(Long partyId) {
        PartyEntryEntity party = partyEntryRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
                
        List<CommissionPaymentEntity> entities = repository.findByParty_IdAndStatusOrderByPaymentDateDescCommissionPaymentIdDesc(partyId, "ACTIVE");
        List<CommissionPaymentResponse> payments = entities.stream().map(mapper::toResponse).collect(Collectors.toList());
        
        BigDecimal earned = commissionCalculationService.getTotalCommissionEarned(partyId);
        BigDecimal paid = repository.sumActivePaymentAmountByPartyId(partyId);
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
        
        return res;
    }

    @Override
    @Transactional
    public CommissionPaymentResponse update(Long id, CommissionPaymentRequest request) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
                
        PartyEntryEntity party = partyEntryRepository.findById(request.getPartyId())
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
                
        BigDecimal totalEarned = commissionCalculationService.getTotalCommissionEarned(party.getId());
        // Calculate paid excluding this specific payment to allow resizing
        BigDecimal paidExcludingCurrent = repository.sumActivePaymentAmountByPartyIdExcluding(party.getId(), id);
        
        BigDecimal maxAllowed = totalEarned.subtract(paidExcludingCurrent);
        
        if (request.getPaymentAmount().compareTo(maxAllowed) > 0) {
            throw new IllegalArgumentException("Updated amount exceeds total outstanding commission");
        }
        
        entity.setParty(party);
        entity.setPaymentDate(request.getPaymentDate());
        entity.setPaymentAmount(request.getPaymentAmount());
        entity.setRemarks(request.getRemarks());
        
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CommissionPaymentEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        // Soft delete
        entity.setStatus("DELETED");
        repository.save(entity);
    }
}
