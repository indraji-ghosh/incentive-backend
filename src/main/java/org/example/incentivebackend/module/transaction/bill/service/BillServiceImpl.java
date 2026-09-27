package org.example.incentivebackend.module.transaction.bill.service;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.transaction.bill.dto.BillAnnexureRequest;
import org.example.incentivebackend.module.transaction.bill.dto.request.BillRequest;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillAnnexureResponse;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillResponse;
import org.example.incentivebackend.module.transaction.bill.entity.BillAnnexureEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.enums.BillPaymentStatus;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.example.incentivebackend.module.transaction.partypayable.service.PartyPayableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final ClientRepository clientRepository;
    private final PartyEntryRepository partyRepository;
    private final SiteRepository siteRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PartyPayableService partyPayableService;

    @Override
    public BillResponse create(BillRequest request) {
        if (billRepository.existsByBillNumberIgnoreCase(request.getBillNumber())) {
            throw new DuplicateResourceException("Bill number already exists");
        }

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found: " + request.getClientId()));

        BillEntity bill = new BillEntity();
        bill.setBillNumber(request.getBillNumber());
        bill.setWorkingMonth(request.getWorkingMonth());
        bill.setClient(client);

        if (request.getPartyId() != null) {
            partyRepository.findById(request.getPartyId()).ifPresent(bill::setParty);
        }

        resolveAndSetSite(request, bill);

        bill.setBillAmount(request.getBillAmount());
        bill.setPaymentStatus(BillPaymentStatus.UNPAID);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setOutstandingAmount(request.getBillAmount());
        bill.setRemarks(request.getRemarks());

        List<BillAnnexureEntity> annexures = new ArrayList<>();
        if (request.getAnnexures() != null) {
            for (BillAnnexureRequest row : request.getAnnexures()) {
                BillAnnexureEntity annexure = new BillAnnexureEntity();
                annexure.setBill(bill);
                annexure.setRrNo(row.getRrNo());
                annexure.setRrDate(row.getRrDate());
                annexure.setChallan(row.getChallan());
                annexure.setLoadDate(row.getLoadDate());
                annexure.setSiding(row.getSiding());
                annexure.setDestination(row.getDestination());
                annexure.setWagons(row.getWagons());
                annexure.setWeight(row.getWeight());
                annexures.add(annexure);
            }
        }
        bill.setAnnexures(annexures);

        if (request.getServiceId() != null) {
            bill.setService(serviceTypeRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found")));
        }

        BillEntity saved = billRepository.save(bill);

        // Generate any applicable BILL_BASED party payables
        partyPayableService.generatePayablesForBill(saved);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse findById(Long id) {
        BillEntity bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
        return toResponse(bill);
    }

    @Override
    public BillResponse update(Long id, BillRequest request) {
        BillEntity bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));

        if (!bill.getBillNumber().equalsIgnoreCase(request.getBillNumber())
                && billRepository.existsByBillNumberIgnoreCase(request.getBillNumber())) {
            throw new DuplicateResourceException("Bill number already exists");
        }

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        bill.setBillNumber(request.getBillNumber());
        bill.setWorkingMonth(request.getWorkingMonth());
        bill.setClient(client);

        if (request.getPartyId() != null) {
            partyRepository.findById(request.getPartyId()).ifPresent(bill::setParty);
        } else {
            bill.setParty(null);
        }

        resolveAndSetSite(request, bill);

        BigDecimal requestAmount = request.getBillAmount() != null ? request.getBillAmount() : (bill.getBillAmount() != null ? bill.getBillAmount() : BigDecimal.ZERO);
        bill.setBillAmount(requestAmount);
        BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal outstanding = requestAmount.subtract(paid);
        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO;
        }
        bill.setOutstandingAmount(outstanding);

        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            bill.setPaymentStatus(BillPaymentStatus.UNPAID);
        } else if (paid.compareTo(requestAmount) >= 0) {
            bill.setPaymentStatus(BillPaymentStatus.PAID);
        } else {
            bill.setPaymentStatus(BillPaymentStatus.PARTIALLY_PAID);
        }

        bill.setRemarks(request.getRemarks());

        bill.getAnnexures().clear();
        if (request.getAnnexures() != null) {
            for (BillAnnexureRequest row : request.getAnnexures()) {
                BillAnnexureEntity annexure = new BillAnnexureEntity();
                annexure.setBill(bill);
                annexure.setRrNo(row.getRrNo());
                annexure.setRrDate(row.getRrDate());
                annexure.setChallan(row.getChallan());
                annexure.setLoadDate(row.getLoadDate());
                annexure.setSiding(row.getSiding());
                annexure.setDestination(row.getDestination());
                annexure.setWagons(row.getWagons());
                annexure.setWeight(row.getWeight());
                bill.getAnnexures().add(annexure);
            }
        }

        if (request.getServiceId() != null) {
            bill.setService(serviceTypeRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found")));
        } else {
            bill.setService(null);
        }

        BillEntity updated = billRepository.save(bill);
        BillEntity target = (updated != null) ? updated : bill;
        partyPayableService.generatePayablesForBill(target);

        return toResponse(target);
    }

    private void resolveAndSetSite(BillRequest request, BillEntity bill) {
        if (request.getSiteId() != null) {
            siteRepository.findById(request.getSiteId()).ifPresent(bill::setSite);
        } else if (request.getAnnexures() != null && !request.getAnnexures().isEmpty()) {
            String siding = request.getAnnexures().get(0).getSiding();
            if (siding != null && !siding.isBlank()) {
                siteRepository.findBySiteNameIgnoreCase(siding.trim())
                        .or(() -> siteRepository.findBySiteShortCodeIgnoreCase(siding.trim()))
                        .ifPresent(bill::setSite);
            }
        }
    }

    @Override
    public void delete(Long id) {
        BillEntity bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
        billRepository.delete(bill);
    }

    private BillResponse toResponse(BillEntity bill) {
        List<BillAnnexureResponse> rows = bill.getAnnexures()
                .stream()
                .map(row -> BillAnnexureResponse.builder()
                        .billAnnexureId(row.getBillAnnexureId())
                        .rrNo(row.getRrNo())
                        .rrDate(row.getRrDate())
                        .challan(row.getChallan())
                        .loadDate(row.getLoadDate())
                        .siding(row.getSiding())
                        .destination(row.getDestination())
                        .wagons(row.getWagons())
                        .weight(row.getWeight())
                        .build())
                .toList();

        int totalWagons = rows.stream()
                .mapToInt(row -> row.getWagons() == null ? 0 : row.getWagons())
                .sum();

        BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal outstanding = bill.getOutstandingAmount() != null ? bill.getOutstandingAmount() : bill.getBillAmount();
        String paymentStatus = bill.getPaymentStatus() != null ? bill.getPaymentStatus().name() : "UNPAID";

        return BillResponse.builder()
                .billId(bill.getBillId())
                .billNumber(bill.getBillNumber())
                .workingMonth(bill.getWorkingMonth())
                .clientId(bill.getClient().getClientId())
                .clientName(bill.getClient().getClientName())
                .partyId(bill.getParty() != null ? bill.getParty().getId() : null)
                .partyName(bill.getParty() != null ? bill.getParty().getPartyName() : null)
                .siteId(bill.getSite() != null ? bill.getSite().getSiteId() : null)
                .siteName(bill.getSite() != null ? bill.getSite().getSiteName() : null)
                .siteShortCode(bill.getSite() != null ? bill.getSite().getSiteShortCode() : null)
                .billAmount(bill.getBillAmount())
                .paymentStatus(paymentStatus)
                .paidAmount(paid)
                .outstandingAmount(outstanding)
                .remarks(bill.getRemarks())
                .totalRr(rows.size())
                .totalWagons(totalWagons)
                .annexures(rows)
                .serviceId(bill.getService() != null ? bill.getService().getId() : null)
                .serviceName(bill.getService() != null ? bill.getService().getName() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> findAll(String billNumber, Long clientId, Long siteId, Pageable pageable) {
        Specification<BillEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            
            if (billNumber != null && !billNumber.trim().isEmpty()) {
                p = cb.and(p, cb.like(cb.lower(root.get("billNumber")), "%" + billNumber.trim().toLowerCase() + "%"));
            }
            if (clientId != null) {
                p = cb.and(p, cb.equal(root.get("client").get("clientId"), clientId));
            }
            if (siteId != null) {
                p = cb.and(p, cb.equal(root.get("site").get("siteId"), siteId));
            }
            
            return p;
        };

        return billRepository.findAll(spec, pageable).map(this::toResponse);
    }
}