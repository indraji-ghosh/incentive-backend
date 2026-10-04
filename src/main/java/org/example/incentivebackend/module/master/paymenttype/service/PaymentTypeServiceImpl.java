package org.example.incentivebackend.module.master.paymenttype.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.mapper.PaymentTypeMapper;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTypeServiceImpl implements PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final PaymentTypeMapper paymentTypeMapper;

    @Override
    public PaymentTypeResponse create(PaymentTypeRequest request) {
        if (paymentTypeRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateResourceException("Payment Type code already exists.");
        }

        PaymentTypeEntity entity = paymentTypeMapper.toEntity(request);
        PaymentTypeEntity saved = paymentTypeRepository.save(entity);
        return paymentTypeMapper.toResponse(saved);
    }

    @Override
    public PaymentTypeResponse update(Long id, PaymentTypeRequest request) {
        PaymentTypeEntity entity = paymentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + id));

        // Code update is ignored by mapper, so no need to check duplicate code on update if we don't allow changing it
        paymentTypeMapper.updateEntity(request, entity);
        PaymentTypeEntity updated = paymentTypeRepository.save(entity);
        return paymentTypeMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentTypeResponse findById(Long id) {
        PaymentTypeEntity entity = paymentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + id));
        return paymentTypeMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentTypeResponse> findAll(String search, Pageable pageable) {
        Specification<PaymentTypeEntity> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), likePattern),
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern)
            ));
        }

        return paymentTypeRepository.findAll(spec, pageable)
                .map(paymentTypeMapper::toResponse);
    }

    @Override
    public void delete(Long id) {
        PaymentTypeEntity entity = paymentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + id));
        // Placeholder for in-use check
        paymentTypeRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DropdownDTO> getDropdown() {
        return paymentTypeRepository.findAll().stream()
                .map(pt -> DropdownDTO.builder()
                        .value(pt.getId())
                        .label("[" + pt.getCode() + "] " + pt.getName())
                        .build())
                .toList();
    }
}
