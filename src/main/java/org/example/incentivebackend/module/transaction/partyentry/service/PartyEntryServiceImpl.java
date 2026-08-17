package org.example.incentivebackend.module.transaction.partyentry.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.businessHead.repository.BusinessHeadRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyUnitConfigurationEntity;
import org.example.incentivebackend.module.transaction.partyentry.mapper.PartyEntryMapper;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyEntryServiceImpl implements PartyEntryService {

    private final PartyEntryRepository partyEntryRepository;
    private final PartyEntryMapper partyEntryMapper;
    
    private final BusinessHeadRepository businessHeadRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final ClientRepository clientRepository;
    private final SiteRepository siteRepository;
    private final UnitRepository unitRepository;

    @Override
    public PartyEntryResponse create(PartyEntryRequest request) {
        PartyEntryEntity entity = partyEntryMapper.toEntity(request);
        entity.setAppStatus(StatusEnum.A.name());
        
        mapRelationships(request, entity);
        
        PartyEntryEntity saved = partyEntryRepository.save(entity);
        return partyEntryMapper.toResponse(saved);
    }

    @Override
    public PartyEntryResponse update(Long id, PartyEntryRequest request) {
        PartyEntryEntity entity = partyEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Entry not found with id: " + id));

        partyEntryMapper.updateEntity(request, entity);
        
        // Re-map relationships
        mapRelationships(request, entity);
        
        PartyEntryEntity updated = partyEntryRepository.save(entity);
        return partyEntryMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyEntryResponse findById(Long id) {
        PartyEntryEntity entity = partyEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Entry not found with id: " + id));
        return partyEntryMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartyEntryResponse> findAll(String search, Pageable pageable) {
        Specification<PartyEntryEntity> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("partyName")), likePattern),
                    cb.like(cb.lower(root.get("remarks")), likePattern)
            ));
        }

        return partyEntryRepository.findAll(spec, pageable)
                .map(partyEntryMapper::toResponse);
    }

    @Override
    public void delete(Long id) {
        PartyEntryEntity entity = partyEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Entry not found with id: " + id));
        
        // Soft delete or hard delete? Let's hard delete for now per common patterns.
        partyEntryRepository.delete(entity);
    }

    private void mapRelationships(PartyEntryRequest request, PartyEntryEntity entity) {
        // Map Business Head
        BusinessHeadEntity businessHead = businessHeadRepository.findById(request.getBusinessHeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Business Head not found: " + request.getBusinessHeadId()));
        entity.setBusinessHead(businessHead);

        // Map Payment Type
        PaymentTypeEntity paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + request.getPaymentTypeId()));
        entity.setPaymentType(paymentType);

        // Map Clients
        List<ClientEntity> clients = clientRepository.findAllById(request.getClientIds());
        if (clients.size() != request.getClientIds().size()) {
            throw new ResourceNotFoundException("One or more Clients not found.");
        }
        entity.setClients(clients);

        // Map Sites
        List<SiteEntity> sites = siteRepository.findAllById(request.getSiteIds());
        if (sites.size() != request.getSiteIds().size()) {
            throw new ResourceNotFoundException("One or more Sites not found.");
        }
        entity.setSites(sites);

        // Map Unit Configurations
        // For updates, we clear and recreate, or update existing. 
        // For simplicity and to ensure orphan removal works, we'll synchronize the list.
        
        Map<Long, PartyUnitConfigurationEntity> existingUnits = entity.getUnitConfigurations().stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(PartyUnitConfigurationEntity::getId, u -> u));
        
        entity.getUnitConfigurations().clear();

        for (PartyUnitConfigurationRequest unitReq : request.getUnitConfigurations()) {
            UnitEntity unit = unitRepository.findById(unitReq.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitReq.getUnitId()));
            
            PartyUnitConfigurationEntity configEntity;
            if (unitReq.getId() != null && existingUnits.containsKey(unitReq.getId())) {
                configEntity = existingUnits.get(unitReq.getId());
                configEntity.setRate(unitReq.getRate());
                configEntity.setEffectiveFrom(unitReq.getEffectiveFrom());
                configEntity.setEffectiveTo(unitReq.getEffectiveTo());
                configEntity.setNotes(unitReq.getNotes());
            } else {
                configEntity = partyEntryMapper.toEntity(unitReq);
            }
            
            configEntity.setUnit(unit);
            entity.addUnitConfiguration(configEntity);
        }
    }
}
