package org.example.incentivebackend.module.master.unit.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.mapper.UnitMapper;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;

    @Override
    public UnitResponse create(UnitCreateRequest request) {
        if (unitRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateResourceException("Unit code already exists.");
        }

        UnitEntity entity = unitMapper.toEntity(request);
        UnitEntity saved = unitRepository.save(entity);
        return unitMapper.toResponse(saved);
    }

    @Override
    public UnitResponse update(Long id, UnitUpdateRequest request) {
        UnitEntity entity = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));

        unitMapper.updateEntity(request, entity);
        UnitEntity updated = unitRepository.save(entity);
        return unitMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitResponse findById(Long id) {
        UnitEntity entity = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));
        return unitMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UnitResponse> findAll(String search, Pageable pageable) {
        Specification<UnitEntity> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), likePattern),
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            ));
        }

        return unitRepository.findAll(spec, pageable)
                .map(unitMapper::toResponse);
    }

    @Override
    public void delete(Long id) {
        UnitEntity entity = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found."));
        // Placeholder for in-use check
        unitRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownDTO> getDropdown() {
        return unitRepository.findAll().stream()
                .map(u -> DropdownDTO.builder()
                        .value(u.getId())
                        .label(u.getName())
                        .build())
                .toList();
    }
}
