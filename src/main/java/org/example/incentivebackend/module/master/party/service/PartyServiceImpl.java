package org.example.incentivebackend.module.master.party.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.businessHead.repository.BusinessHeadRepository;
import org.example.incentivebackend.module.master.party.dto.request.PartyRequest;
import org.example.incentivebackend.module.master.party.dto.response.PartyResponse;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.mapper.PartyMapper;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final BusinessHeadRepository businessHeadRepository;
    private final PartyMapper partyMapper;

    @Override
    public PartyResponse create(PartyRequest request) {
        if (partyRepository.existsByPartyNameIgnoreCase(request.getPartyName())) {
            throw new DuplicateResourceException("Party already exists with name: " + request.getPartyName());
        }

        PartyEntity entity = partyMapper.toEntity(request);
        if (request.getBusinessHeadId() != null) {
            BusinessHeadEntity businessHead = businessHeadRepository.findById(request.getBusinessHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Business Head not found: " + request.getBusinessHeadId()));
            entity.setBusinessHead(businessHead);
        }

        PartyEntity saved = partyRepository.save(entity);
        return partyMapper.toResponse(saved);
    }

    @Override
    public PartyResponse update(Long id, PartyRequest request) {
        PartyEntity entity = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + id));

        if (partyRepository.existsByPartyNameIgnoreCaseAndIdNot(request.getPartyName(), id)) {
            throw new DuplicateResourceException("Another party already exists with name: " + request.getPartyName());
        }

        partyMapper.updateEntity(request, entity);
        if (request.getBusinessHeadId() != null) {
            BusinessHeadEntity businessHead = businessHeadRepository.findById(request.getBusinessHeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Business Head not found: " + request.getBusinessHeadId()));
            entity.setBusinessHead(businessHead);
        } else {
            entity.setBusinessHead(null);
        }

        PartyEntity updated = partyRepository.save(entity);
        return partyMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyResponse findById(Long id) {
        PartyEntity entity = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + id));
        return partyMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartyResponse> findAll(String search, Pageable pageable) {
        Specification<PartyEntity> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("partyName")), pattern),
                    cb.like(cb.lower(root.get("remarks")), pattern)
            ));
        }
        return partyRepository.findAll(spec, pageable).map(partyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyResponse> findAllActive() {
        return partyMapper.toResponseList(partyRepository.findByPartyStatus(StatusEnum.A));
    }

    @Override
    public void delete(Long id) {
        PartyEntity entity = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + id));
        partyRepository.delete(entity);
    }
}
