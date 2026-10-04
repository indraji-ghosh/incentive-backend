package org.example.incentivebackend.module.master.businessHead.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadRequest;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadResponse;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.businessHead.repository.BusinessHeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessHeadServiceImpl implements BusinessHeadService {

    private final BusinessHeadRepository businessHeadRepository;

    @Override
    public BusinessHeadResponse create(
            BusinessHeadRequest request
    ) {

        if (businessHeadRepository.existsByHeadShortCode(
                request.getHeadShortCode()
        )) {
            throw new RuntimeException(
                    "Business head short code already exists"
            );
        }

        BusinessHeadEntity entity = new BusinessHeadEntity();

        entity.setHeadName(request.getHeadName());
        entity.setHeadShortCode(request.getHeadShortCode());
        entity.setHeadStatus(
                request.getHeadStatus() != null
                        ? request.getHeadStatus()
                        : StatusEnum.A
        );

        BusinessHeadEntity saved =
                businessHeadRepository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessHeadResponse getById(Long id) {

        BusinessHeadEntity entity =
                businessHeadRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Business head not found: " + id
                                )
                        );

        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessHeadResponse> getAll() {

        return businessHeadRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BusinessHeadResponse update(
            Long id,
            BusinessHeadRequest request
    ) {

        BusinessHeadEntity entity =
                businessHeadRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Business head not found: " + id
                                )
                        );

        if (!entity.getHeadShortCode()
                .equals(request.getHeadShortCode())
                && businessHeadRepository.existsByHeadShortCode(
                request.getHeadShortCode()
        )) {

            throw new RuntimeException(
                    "Business head short code already exists"
            );
        }

        entity.setHeadName(request.getHeadName());
        entity.setHeadShortCode(request.getHeadShortCode());

        if (request.getHeadStatus() != null) {
            entity.setHeadStatus(request.getHeadStatus());
        }

        BusinessHeadEntity updated =
                businessHeadRepository.save(entity);

        return mapToResponse(updated);
    }

    @Override
    public void delete(Long id) {

        BusinessHeadEntity entity =
                businessHeadRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Business head not found: " + id
                                )
                        );

        businessHeadRepository.delete(entity);
    }

    private BusinessHeadResponse mapToResponse(
            BusinessHeadEntity entity
    ) {

        return BusinessHeadResponse.builder()
                .headId(entity.getHeadId())
                .headName(entity.getHeadName())
                .headShortCode(entity.getHeadShortCode())
                .headStatus(entity.getHeadStatus())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}