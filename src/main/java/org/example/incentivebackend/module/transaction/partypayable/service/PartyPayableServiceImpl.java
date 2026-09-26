package org.example.incentivebackend.module.transaction.partypayable.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyAssignmentRepository;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyServiceConfigurationRepository;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.transaction.bill.entity.BillAnnexureEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.transaction.partypayable.dto.request.PartyPayableFilter;
import org.example.incentivebackend.module.transaction.partypayable.dto.response.PartyPayableResponse;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.mapper.PartyPayableMapper;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PartyPayableServiceImpl implements PartyPayableService {

    private final PartyPayableRepository partyPayableRepository;
    private final PartyAssignmentRepository partyAssignmentRepository;
    private final PartyServiceConfigurationRepository configurationRepository;
    private final SiteRepository siteRepository;
    private final BillRepository billRepository;
    private final PartyPayableMapper mapper;

    @Override
    public List<PartyPayableEntity> generatePayablesForRake(RakeEntryEntity rake) {
        if (rake == null || rake.getRakeEntryId() == null || rake.getClient() == null) {
            return List.of();
        }

        Long clientId = rake.getClient().getClientId();
        Long siteId = resolveSiteIdForRake(rake);

        LocalDate transactionDate = rake.getWorkingMonth();
        if (rake.getAnnexures() != null && !rake.getAnnexures().isEmpty()) {
            RakeAnnexureEntity first = rake.getAnnexures().get(0);
            if (first.getRrDate() != null) {
                transactionDate = first.getRrDate();
            } else if (first.getLoadDate() != null) {
                transactionDate = first.getLoadDate();
            }
        }

        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }

        log.info("Generating party payables for Rake ID {} (Number: {}, Client ID: {}, Site ID: {}, Date: {})",
                rake.getRakeEntryId(), rake.getRakeNumber(), clientId, siteId, transactionDate);

        // Aggregate rake metrics across annexures
        int totalWagons = 0;
        BigDecimal totalWeight = BigDecimal.ZERO;
        String primaryRrNo = (rake.getRakeNumber() != null && !rake.getRakeNumber().isBlank())
                ? rake.getRakeNumber()
                : null;

        if (rake.getAnnexures() != null) {
            for (RakeAnnexureEntity ann : rake.getAnnexures()) {
                if (ann.getWagons() != null) {
                    totalWagons += ann.getWagons();
                }
                if (ann.getWeight() != null) {
                    totalWeight = totalWeight.add(ann.getWeight());
                }
                if (primaryRrNo == null && ann.getRrNo() != null && !ann.getRrNo().isBlank()) {
                    primaryRrNo = ann.getRrNo();
                }
            }
        }

        // Find applicable Party Assignments for Client + Site
        List<PartyAssignmentEntity> assignments;
        if (siteId != null) {
            assignments = partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(clientId, siteId, StatusEnum.A);
        } else {
            assignments = partyAssignmentRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("client").get("clientId"), clientId),
                    cb.equal(root.get("status"), StatusEnum.A)
            ));
        }

        if (assignments.isEmpty()) {
            log.warn("No active Party Assignment found for Rake ID {} with Client ID {} and Site ID {}. Ensure Party Assignment is configured for this Client + Site.",
                    rake.getRakeEntryId(), clientId, siteId);
            return List.of();
        }

        // Check if Rake specifies performed services
        List<Long> performedServiceIds = (rake.getServices() != null && !rake.getServices().isEmpty())
                ? rake.getServices().stream().map(ServiceTypeEntity::getId).toList()
                : null;

        List<PartyPayableEntity> generatedPayables = new ArrayList<>();
        String sourceId = String.valueOf(rake.getRakeEntryId());

        for (PartyAssignmentEntity assignment : assignments) {
            // If the Rake specifies a specific party, filter for that party; otherwise apply to all assigned parties
            if (rake.getParty() != null && !rake.getParty().getId().equals(assignment.getParty().getId())) {
                continue;
            }

            List<PartyServiceConfigurationEntity> effectiveConfigs =
                    configurationRepository.findEffectiveConfigurations(assignment.getId(), transactionDate, StatusEnum.A);

            // Fallback: If no config found on exact transaction date, check if active configuration exists in the same calendar month
            if (effectiveConfigs.isEmpty()) {
                LocalDate endOfMonth = transactionDate.withDayOfMonth(transactionDate.lengthOfMonth());
                effectiveConfigs = configurationRepository.findEffectiveConfigurations(assignment.getId(), endOfMonth, StatusEnum.A);
            }

            if (effectiveConfigs.isEmpty()) {
                log.warn("Party Assignment ID {} (Party: {}) has no effective service configurations for transaction date {}. Skipping.",
                        assignment.getId(), assignment.getParty().getPartyName(), transactionDate);
                continue;
            }

            for (PartyServiceConfigurationEntity config : effectiveConfigs) {
                Long serviceId = config.getService().getId();

                // If rake specifies performed services, only generate for matching services
                if (performedServiceIds != null && !performedServiceIds.contains(serviceId)) {
                    continue;
                }

                String paymentCode = config.getPaymentType().getCode().toUpperCase();
                String unitCode = config.getUnit() != null ? config.getUnit().getCode().toUpperCase() : "";

                // Skip non-rake payment types for rake transactions
                if (paymentCode.contains("MONTHLY") || paymentCode.contains("BILL")) {
                    continue;
                }

                // Idempotency check: avoid duplicate payable generation for the same transaction & service configuration
                if (partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                        "RAKE", sourceId, assignment.getId(), serviceId)) {
                    log.info("Payable already exists for Rake {}, Assignment {}, Service {}. Skipping.",
                            sourceId, assignment.getId(), serviceId);
                    continue;
                }

                BigDecimal quantity = BigDecimal.ONE;
                BigDecimal payableAmount;

                if (paymentCode.contains("WAGON") || unitCode.contains("WAGON")) {
                    quantity = BigDecimal.valueOf(totalWagons);
                    payableAmount = config.getRate().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                } else if (paymentCode.contains("MT") || paymentCode.contains("METRIC") || unitCode.contains("MT") || unitCode.contains("METRIC")) {
                    quantity = totalWeight;
                    payableAmount = config.getRate().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                } else {
                    // Default to RAKE_BASED (quantity = 1)
                    quantity = BigDecimal.ONE;
                    payableAmount = config.getRate().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
                }

                PartyPayableEntity payable = new PartyPayableEntity();
                payable.setParty(assignment.getParty());
                payable.setPartyAssignment(assignment);
                payable.setService(config.getService());
                payable.setPartyServiceConfiguration(config);
                payable.setSourceType("RAKE");
                payable.setSourceId(sourceId);
                payable.setSourceReference(primaryRrNo != null ? (primaryRrNo.startsWith("RK-") ? primaryRrNo : "RR-" + primaryRrNo) : "RAKE-" + sourceId);
                payable.setCalculationBasis(paymentCode);
                payable.setQuantity(quantity);
                payable.setRate(config.getRate());
                payable.setPayableAmount(payableAmount);
                payable.setTransactionDate(transactionDate);
                payable.setStatus(StatusEnum.A);
                payable.setRemarks("Auto-generated for Rake " + sourceId + " (" + paymentCode + ")");

                PartyPayableEntity saved = partyPayableRepository.save(payable);
                log.info("Successfully generated Party Payable ID: {} for Party: {} (Amount: ₹{}, Basis: {})",
                        saved.getId(), saved.getParty().getPartyName(), saved.getPayableAmount(), paymentCode);
                generatedPayables.add(saved);
            }
        }

        return generatedPayables;
    }

    @Override
    public List<PartyPayableEntity> generatePayablesForBillId(Long billId) {
        if (billId == null) return List.of();
        BillEntity bill = billRepository.findById(billId).orElse(null);
        if (bill == null) {
            log.warn("Bill ID {} not found for party payable generation", billId);
            return List.of();
        }
        return generatePayablesForBill(bill);
    }

    @Override
    public List<PartyPayableResponse> generateAndGetResponsesForBillId(Long billId) {
        List<PartyPayableEntity> payables = generatePayablesForBillId(billId);
        return payables.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<PartyPayableEntity> generatePayablesForBill(BillEntity bill) {
        if (bill == null || bill.getBillId() == null || bill.getClient() == null) {
            return List.of();
        }

        Long clientId = bill.getClient().getClientId();
        Long siteId = bill.getSite() != null ? bill.getSite().getSiteId() : null;
        LocalDate transactionDate = bill.getWorkingMonth() != null 
                ? bill.getWorkingMonth().withDayOfMonth(bill.getWorkingMonth().lengthOfMonth()) 
                : LocalDate.now();

        log.info("Generating party payables for Bill ID {} (Number: {}, Client ID: {}, Site ID: {}, Month: {})",
                bill.getBillId(), bill.getBillNumber(), clientId, siteId, transactionDate);

        List<PartyAssignmentEntity> assignments;
        if (siteId != null) {
            assignments = partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(clientId, siteId, StatusEnum.A);
        } else {
            assignments = partyAssignmentRepository.findAll((root, query, cb) -> cb.and(
                    cb.equal(root.get("client").get("clientId"), clientId),
                    cb.equal(root.get("status"), StatusEnum.A)
            ));
        }

        if (assignments.isEmpty()) {
            log.warn("No active Party Assignment found for Bill ID {} with Client ID {} and Site ID {}. Ensure Party Assignment is configured.",
                    bill.getBillId(), clientId, siteId);
            return List.of();
        }

        List<PartyPayableEntity> generatedPayables = new ArrayList<>();
        String sourceId = String.valueOf(bill.getBillId());

        // Aggregate metrics from bill annexures if present
        int annexureCount = (bill.getAnnexures() != null && !bill.getAnnexures().isEmpty()) ? bill.getAnnexures().size() : 1;
        int totalWagons = 0;
        if (bill.getAnnexures() != null) {
            for (BillAnnexureEntity annexure : bill.getAnnexures()) {
                if (annexure.getWagons() != null) {
                    totalWagons += annexure.getWagons();
                }
            }
        }

        for (PartyAssignmentEntity assignment : assignments) {
            // If bill specified a party, restrict to that party; otherwise apply to all assigned parties
            if (bill.getParty() != null && !bill.getParty().getId().equals(assignment.getParty().getId())) {
                continue;
            }

            List<PartyServiceConfigurationEntity> effectiveConfigs =
                    configurationRepository.findEffectiveConfigurations(assignment.getId(), transactionDate, StatusEnum.A);

            if (effectiveConfigs.isEmpty()) {
                // Fallback to all active configs for the assignment if date-range query returned empty
                effectiveConfigs = configurationRepository.findByPartyAssignment_IdAndStatus(assignment.getId(), StatusEnum.A);
            }

            if (effectiveConfigs.isEmpty()) {
                log.warn("Party Assignment ID {} (Party: {}) has no effective configurations. Skipping.",
                        assignment.getId(), assignment.getParty().getPartyName());
                continue;
            }

            for (PartyServiceConfigurationEntity config : effectiveConfigs) {
                String paymentCode = config.getPaymentType().getCode().toUpperCase();
                String unitCode = config.getUnit() != null ? config.getUnit().getCode().toUpperCase() : "";

                // Skip MONTHLY payment types (handled by monthly fixed schedule)
                if (paymentCode.contains("MONTHLY")) {
                    continue;
                }

                // If config is BILL_PAYMENT, only generate when bill has received payment
                if (paymentCode.contains("BILL_PAYMENT")) {
                    if (bill.getPaidAmount() == null || bill.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        log.info("Config {} is BILL_PAYMENT but Bill {} has no payments yet. Skipping.",
                                config.getId(), bill.getBillId());
                        continue;
                    }
                }

                // Idempotency check: avoid duplicate payable generation for the same source bill & service configuration
                if (partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                        "BILL", sourceId, assignment.getId(), config.getService().getId())) {
                    log.info("Payable already exists for Bill {}, Assignment {}, Service {}. Skipping.",
                            sourceId, assignment.getId(), config.getService().getId());
                    continue;
                }

                BigDecimal quantity = BigDecimal.ONE;
                if (paymentCode.contains("WAGON") || unitCode.contains("WAGON")) {
                    quantity = BigDecimal.valueOf(totalWagons > 0 ? totalWagons : 1);
                } else if (paymentCode.contains("MT") || paymentCode.contains("METRIC") || unitCode.contains("MT") || unitCode.contains("METRIC")) {
                    // For Bills, weight isn't natively aggregated here easily, defaulting to 1 for MT if not implemented
                    quantity = BigDecimal.ONE;
                } else if (paymentCode.contains("RAKE") || unitCode.contains("RAKE")) {
                    quantity = BigDecimal.valueOf(annexureCount > 0 ? annexureCount : 1);
                } else {
                    quantity = BigDecimal.ONE;
                }

                BigDecimal payableAmount = config.getRate().multiply(quantity).setScale(2, RoundingMode.HALF_UP);

                PartyPayableEntity payable = new PartyPayableEntity();
                payable.setParty(assignment.getParty());
                payable.setPartyAssignment(assignment);
                payable.setService(config.getService());
                payable.setPartyServiceConfiguration(config);
                payable.setSourceType("BILL");
                payable.setSourceId(sourceId);
                payable.setSourceReference(bill.getBillNumber() != null ? "BILL-" + bill.getBillNumber() : "BILL-" + sourceId);
                payable.setCalculationBasis(paymentCode);
                payable.setQuantity(quantity);
                payable.setRate(config.getRate());
                payable.setPayableAmount(payableAmount);
                payable.setTransactionDate(transactionDate);
                payable.setStatus(StatusEnum.A);
                payable.setRemarks("Auto-generated for Bill " + bill.getBillNumber() + " (" + paymentCode + ")");

                PartyPayableEntity saved = partyPayableRepository.save(payable);
                log.info("Successfully generated Party Payable ID: {} for Party: {} (Amount: ₹{}, Basis: {})",
                        saved.getId(), saved.getParty().getPartyName(), saved.getPayableAmount(), paymentCode);
                generatedPayables.add(saved);
            }
        }

        return generatedPayables;
    }

    @Override
    public List<PartyPayableEntity> createMonthlyFixedPayables(LocalDate monthDate) {
        LocalDate date = monthDate != null ? monthDate : LocalDate.now();
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        String periodKey = String.format("%04d-%02d", firstOfMonth.getYear(), firstOfMonth.getMonthValue());

        List<PartyServiceConfigurationEntity> monthlyConfigs =
                configurationRepository.findActiveMonthlyFixedConfigurations(firstOfMonth, StatusEnum.A);

        List<PartyPayableEntity> createdPayables = new ArrayList<>();

        for (PartyServiceConfigurationEntity config : monthlyConfigs) {
            PartyAssignmentEntity assignment = config.getPartyAssignment();

            // Idempotency check: prevent duplicate generation for the same financial month
            if (partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                    "MONTHLY_FIXED", periodKey, assignment.getId(), config.getService().getId())) {
                log.info("Monthly payable already generated for period {}, assignment {}, service {}. Skipping.",
                        periodKey, assignment.getId(), config.getService().getId());
                continue;
            }

            PartyPayableEntity payable = new PartyPayableEntity();
            payable.setParty(assignment.getParty());
            payable.setPartyAssignment(assignment);
            payable.setService(config.getService());
            payable.setPartyServiceConfiguration(config);
            payable.setSourceType("MONTHLY_FIXED");
            payable.setSourceId(periodKey);
            payable.setSourceReference("MONTHLY-" + periodKey);
            payable.setCalculationBasis("MONTHLY_FIXED");
            payable.setQuantity(BigDecimal.ONE);
            payable.setRate(config.getRate());
            payable.setPayableAmount(config.getRate().setScale(2, RoundingMode.HALF_UP));
            payable.setTransactionDate(firstOfMonth);
            payable.setStatus(StatusEnum.A);
            payable.setRemarks("Fixed monthly payable for period " + periodKey);

            PartyPayableEntity saved = partyPayableRepository.save(payable);
            createdPayables.add(saved);
        }

        return createdPayables;
    }

    @Override
    @Transactional(readOnly = true)
    public PartyPayableResponse findById(Long id) {
        PartyPayableEntity entity = partyPayableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Payable not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartyPayableResponse> findAll(PartyPayableFilter filter, Pageable pageable) {
        Specification<PartyPayableEntity> spec = (root, query, cb) -> cb.conjunction();
        if (filter != null) {
            if (filter.getPartyId() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("party").get("id"), filter.getPartyId()));
            }
            if (filter.getClientId() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("partyAssignment").get("client").get("clientId"), filter.getClientId()));
            }
            if (filter.getSiteId() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("partyAssignment").get("site").get("siteId"), filter.getSiteId()));
            }
            if (filter.getServiceId() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("service").get("id"), filter.getServiceId()));
            }
            if (filter.getSourceType() != null && !filter.getSourceType().isBlank()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("sourceType"), filter.getSourceType()));
            }
            if (filter.getFromDate() != null) {
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getToDate()));
            }
            if (filter.getStatus() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
            }
        }
        return partyPayableRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyPayableResponse> findByParty(Long partyId) {
        return mapper.toResponseList(partyPayableRepository.findByParty_IdAndStatusAndPaymentStatusIn(partyId, StatusEnum.A, List.of(org.example.incentivebackend.common.enums.PaymentStatusEnum.UNPAID, org.example.incentivebackend.common.enums.PaymentStatusEnum.PARTIALLY_PAID)));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPayableEarned(Long partyId) {
        return partyPayableRepository.sumPayableAmountByPartyIdAndStatus(partyId, StatusEnum.A);
    }

    private Long resolveSiteIdForRake(RakeEntryEntity rake) {
        if (rake.getSite() != null && rake.getSite().getSiteId() != null) {
            return rake.getSite().getSiteId();
        }

        if (rake.getAnnexures() != null) {
            for (RakeAnnexureEntity ann : rake.getAnnexures()) {
                if (ann.getSiding() != null && !ann.getSiding().isBlank()) {
                    String siding = ann.getSiding().trim();
                    Optional<SiteEntity> site = siteRepository.findBySiteNameIgnoreCase(siding);
                    if (site.isPresent()) {
                        return site.get().getSiteId();
                    }
                    Optional<SiteEntity> siteCode = siteRepository.findBySiteShortCodeIgnoreCase(siding);
                    if (siteCode.isPresent()) {
                        return siteCode.get().getSiteId();
                    }
                }
            }
        }

        return null;
    }
}
