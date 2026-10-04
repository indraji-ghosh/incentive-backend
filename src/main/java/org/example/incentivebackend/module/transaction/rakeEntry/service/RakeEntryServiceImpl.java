package org.example.incentivebackend.module.transaction.rakeEntry.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.example.incentivebackend.module.transaction.partypayable.service.PartyPayableService;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeAnnexureRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.mapper.RakeEntryMapper;
import org.example.incentivebackend.module.transaction.rakeEntry.repository.RakeEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RakeEntryServiceImpl implements RakeEntryService {

    private final RakeEntryRepository rakeEntryRepository;
    private final ClientRepository clientRepository;
    private final PartyEntryRepository partyRepository;
    private final SiteRepository siteRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PartyPayableService partyPayableService;
    private final RakeEntryMapper rakeEntryMapper;
    private final org.example.incentivebackend.common.audit.service.AuditLogService auditLogService;
    private final org.example.incentivebackend.common.audit.util.AuditHelper auditHelper;

    @Override
    public RakeEntryResponse create(RakeEntryRequest request) {
        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        PartyEntryEntity party = null;
        if (request.getPartyId() != null) {
            party = partyRepository.findById(request.getPartyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));
        }

        RakeEntryEntity rakeEntry = rakeEntryMapper.toEntity(request);
        if (rakeEntry == null) {
            rakeEntry = new RakeEntryEntity();
        }
        rakeEntry.setClient(client);
        rakeEntry.setParty(party);

        if (request.getRakeNumber() != null && !request.getRakeNumber().isBlank()) {
            rakeEntry.setRakeNumber(request.getRakeNumber().trim());
        } else if (request.getAnnexures() != null && !request.getAnnexures().isEmpty()) {
            rakeEntry.setRakeNumber(request.getAnnexures().get(0).getRrNo());
        }

        resolveAndSetSite(request, rakeEntry);

        if (request.getServiceId() != null) {
            rakeEntry.setService(serviceTypeRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found")));
        }

        rakeEntry.setRakeStatus(StatusEnum.A);

        if (request.getAnnexures() != null) {
            for (RakeAnnexureRequest annexureReq : request.getAnnexures()) {
                RakeAnnexureEntity annexure = rakeEntryMapper.toAnnexureEntity(annexureReq);
                annexure.setRakeEntry(rakeEntry);
                rakeEntry.getAnnexures().add(annexure);
            }
        }

        RakeEntryEntity saved = rakeEntryRepository.save(rakeEntry);
        RakeEntryEntity target = (saved != null) ? saved : rakeEntry;

        // Auto-generate Party Payables for matching client, site, and services
        partyPayableService.generatePayablesForRake(target);

        RakeEntryResponse response = rakeEntryMapper.toResponse(target);
        
        auditLogService.createAuditLog(
            "TRANSACTION", "RakeEntry", "td_rake_entry", target.getRakeEntryId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.CREATE,
            null, auditHelper.toJson(response),
            "Rake Entry created", 1L,
            target.getRakeNumber(), null, null, null, "SUCCESS"
        );

        return response;
    }



    @Override
    @Transactional(readOnly = true)
    public List<RakeEntryResponse> findAll(String rakeNumber, Long clientId, Long siteId, Long serviceId) {
        Specification<RakeEntryEntity> spec = (root, query, cb) -> {
            Predicate p = cb.conjunction();
            
            // To avoid duplicate rows when joining services
            if (query != null) {
                query.distinct(true);
            }
            
            if (rakeNumber != null && !rakeNumber.isBlank()) {
                p = cb.and(p, cb.like(cb.lower(root.get("rakeNumber")), "%" + rakeNumber.trim().toLowerCase() + "%"));
            }
            if (clientId != null) {
                p = cb.and(p, cb.equal(root.get("client").get("clientId"), clientId));
            }
            if (siteId != null) {
                p = cb.and(p, cb.equal(root.get("site").get("siteId"), siteId));
            }
            if (serviceId != null) {
                Join<RakeEntryEntity, org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity> serviceJoin = root.join("service", JoinType.INNER);
                p = cb.and(p, cb.equal(serviceJoin.get("id"), serviceId));
            }
            return p;
        };
        return rakeEntryMapper.toResponseList(rakeEntryRepository.findAll(spec));
    }

    @Override
    @Transactional(readOnly = true)
    public RakeEntryResponse findById(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        return rakeEntryMapper.toResponse(rakeEntry);
    }

    @Override
    public RakeEntryResponse update(Long id, RakeEntryRequest request) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
                
        String oldStateJson = auditHelper.toJson(rakeEntryMapper.toResponse(rakeEntry));

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        rakeEntryMapper.updateEntityFromRequest(request, rakeEntry);
        rakeEntry.setClient(client);

        if (request.getPartyId() != null) {
            PartyEntryEntity party = partyRepository.findById(request.getPartyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Party not found: " + request.getPartyId()));
            rakeEntry.setParty(party);
        } else {
            rakeEntry.setParty(null);
        }

        if (request.getRakeNumber() != null && !request.getRakeNumber().isBlank()) {
            rakeEntry.setRakeNumber(request.getRakeNumber().trim());
        }

        resolveAndSetSite(request, rakeEntry);

        if (request.getServiceId() != null) {
            rakeEntry.setService(serviceTypeRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found")));
        }

        if (request.getAnnexures() != null) {
            java.util.List<Long> requestedIds = request.getAnnexures().stream()
                    .filter(a -> a.getRakeAnnexureId() != null)
                    .map(RakeAnnexureRequest::getRakeAnnexureId)
                    .toList();

            rakeEntry.getAnnexures().removeIf(a -> a.getRakeAnnexureId() != null && !requestedIds.contains(a.getRakeAnnexureId()));

            for (RakeAnnexureRequest annexureReq : request.getAnnexures()) {
                if (annexureReq.getRakeAnnexureId() != null) {
                    RakeAnnexureEntity existing = rakeEntry.getAnnexures().stream()
                            .filter(a -> annexureReq.getRakeAnnexureId().equals(a.getRakeAnnexureId()))
                            .findFirst()
                            .orElse(null);
                    if (existing != null) {
                        rakeEntryMapper.updateAnnexureEntityFromRequest(annexureReq, existing);
                    } else {
                        RakeAnnexureEntity annexure = rakeEntryMapper.toAnnexureEntity(annexureReq);
                        annexure.setRakeEntry(rakeEntry);
                        rakeEntry.getAnnexures().add(annexure);
                    }
                } else {
                    RakeAnnexureEntity annexure = rakeEntryMapper.toAnnexureEntity(annexureReq);
                    annexure.setRakeEntry(rakeEntry);
                    rakeEntry.getAnnexures().add(annexure);
                }
            }
        } else {
            rakeEntry.getAnnexures().clear();
        }

        RakeEntryEntity updated = rakeEntryRepository.save(rakeEntry);
        RakeEntryEntity target = (updated != null) ? updated : rakeEntry;

        partyPayableService.generatePayablesForRake(target);

        RakeEntryResponse response = rakeEntryMapper.toResponse(target);
        
        auditLogService.createAuditLog(
            "TRANSACTION", "RakeEntry", "td_rake_entry", target.getRakeEntryId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.UPDATE,
            oldStateJson, auditHelper.toJson(response),
            "Rake Entry updated", 1L,
            target.getRakeNumber(), null, null, null, "SUCCESS"
        );

        return response;
    }

    private void resolveAndSetSite(RakeEntryRequest request, RakeEntryEntity rakeEntry) {
        if (request.getSiteId() != null) {
            siteRepository.findById(request.getSiteId()).ifPresent(rakeEntry::setSite);
        } else if (request.getAnnexures() != null && !request.getAnnexures().isEmpty()) {
            String siding = request.getAnnexures().get(0).getSiding();
            if (siding != null && !siding.isBlank()) {
                siteRepository.findBySiteNameIgnoreCase(siding.trim())
                        .or(() -> siteRepository.findBySiteShortCodeIgnoreCase(siding.trim()))
                        .ifPresent(rakeEntry::setSite);
            }
        }
    }

    @Override
    public void softDelete(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        
        String oldStateJson = auditHelper.toJson(rakeEntryMapper.toResponse(rakeEntry));
        rakeEntry.setRakeStatus(StatusEnum.I);
        
        auditLogService.createAuditLog(
            "TRANSACTION", "RakeEntry", "td_rake_entry", id,
            org.example.incentivebackend.common.audit.enums.AuditAction.SOFT_DELETE,
            oldStateJson, auditHelper.toJson(rakeEntryMapper.toResponse(rakeEntry)),
            "Rake Entry soft deleted", 1L,
            rakeEntry.getRakeNumber(), null, null, null, "SUCCESS"
        );
    }

    @Override
    public void hardDelete(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        
        String oldStateJson = auditHelper.toJson(rakeEntryMapper.toResponse(rakeEntry));
        rakeEntryRepository.delete(rakeEntry);
        
        auditLogService.createAuditLog(
            "TRANSACTION", "RakeEntry", "td_rake_entry", id,
            org.example.incentivebackend.common.audit.enums.AuditAction.DELETE,
            oldStateJson, null,
            "Rake Entry hard deleted", 1L,
            rakeEntry.getRakeNumber(), null, null, null, "SUCCESS"
        );
    }
}
