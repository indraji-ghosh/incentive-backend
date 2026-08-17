package org.example.incentivebackend.module.master.sector.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.sector.dto.SectorFilter;
import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.example.incentivebackend.module.master.sector.entity.SectorEntity;
import org.example.incentivebackend.module.master.sector.mapper.SectorMapper;
import org.example.incentivebackend.module.master.sector.repository.SectorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;
    private final SectorMapper sectorMapper;

    @Override
    public SectorResponse create(SectorRequest request) {

        if (sectorRepository.existsBySectorNameIgnoreCase(
                request.getSectorName()
        )) {
            throw new DuplicateResourceException(
                    "Sector name already exists"
            );
        }

        if (sectorRepository.existsBySectorShortCodeIgnoreCase(
                request.getSectorShortCode()
        )) {
            throw new DuplicateResourceException(
                    "Sector short code already exists"
            );
        }

        SectorEntity entity =
                sectorMapper.toEntity(request);

        if (entity.getSectorStatus() == null) {
            entity.setSectorStatus(StatusEnum.A);
        }

        SectorEntity saved =
                sectorRepository.save(entity);

        return sectorMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SectorResponse> findAll(
            SectorFilter filter
    ) {

        Sort.Direction direction =
                "asc".equalsIgnoreCase(
                        filter.getSortDirection()
                )
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        Sort sort = Sort.by(
                direction,
                filter.getSortBy()
        );

        Pageable pageable =
                PageRequest.of(
                        filter.getPage(),
                        filter.getSize(),
                        sort
                );

        String search = filter.getSearch();

        if (search != null && search.isBlank()) {
            search = null;
        }

        Page<SectorEntity> sectors =
                sectorRepository.findAll(
                        search,
                        filter.getSectorStatus(),
                        pageable
                );

        return sectors.map(
                sectorMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SectorResponse findById(Long id) {

        SectorEntity entity =
                sectorRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sector not found: " + id
                                )
                        );

        return sectorMapper.toResponse(entity);
    }

    @Override
    public SectorResponse update(
            Long id,
            SectorRequest request
    ) {

        SectorEntity entity =
                sectorRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sector not found: " + id
                                )
                        );

        if (sectorRepository
                .existsBySectorNameIgnoreCaseAndSectorIdNot(
                        request.getSectorName(),
                        id
                )) {

            throw new DuplicateResourceException(
                    "Sector name already exists"
            );
        }

        if (sectorRepository
                .existsBySectorShortCodeIgnoreCaseAndSectorIdNot(
                        request.getSectorShortCode(),
                        id
                )) {

            throw new DuplicateResourceException(
                    "Sector short code already exists"
            );
        }

        sectorMapper.updateEntity(
                request,
                entity
        );

        SectorEntity updated =
                sectorRepository.save(entity);

        return sectorMapper.toResponse(updated);
    }

    @Override
    public void softDelete(Long id) {

        SectorEntity entity =
                sectorRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sector not found: " + id
                                )
                        );

        entity.setSectorStatus(StatusEnum.I);

        sectorRepository.save(entity);
    }

    @Override
    public void hardDelete(Long id) {

        SectorEntity entity =
                sectorRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sector not found: " + id
                                )
                        );

        sectorRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectorResponse> findActive() {

        return sectorMapper.toResponseList(
                sectorRepository.findBySectorStatus(
                        StatusEnum.A
                )
        );
    }
}