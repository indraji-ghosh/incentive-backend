package org.example.incentivebackend.module.master.site.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.site.dto.SiteFilter;
import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.mapper.SiteMapper;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
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
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;

    @Override
    public SiteResponse create(SiteRequest request) {

        if (siteRepository.existsBySiteShortCodeIgnoreCase(
                request.getSiteShortCode()
        )) {
            throw new DuplicateResourceException(
                    "Site short code already exists"
            );
        }

        SiteEntity entity =
                siteMapper.toEntity(request);

        if (entity.getSiteStatus() == null) {
            entity.setSiteStatus(StatusEnum.A);
        }

        SiteEntity saved =
                siteRepository.save(entity);

        return siteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SiteResponse> findAll(
            SiteFilter filter
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

        String search = normalize(filter.getSearch());
        String state = normalize(filter.getState());
        StatusEnum status = filter.getSiteStatus();

        Page<SiteEntity> sites =
                siteRepository.findAll(
                        search,
                        state,
                        status,
                        pageable
                );

        return sites.map(
                siteMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse findById(Long id) {

        SiteEntity entity =
                siteRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found: " + id
                                )
                        );

        return siteMapper.toResponse(entity);
    }

    @Override
    public SiteResponse update(
            Long id,
            SiteRequest request
    ) {

        SiteEntity entity =
                siteRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found: " + id
                                )
                        );

        if (siteRepository
                .existsBySiteShortCodeIgnoreCaseAndSiteIdNot(
                        request.getSiteShortCode(),
                        id
                )) {

            throw new DuplicateResourceException(
                    "Site short code already exists"
            );
        }

        siteMapper.updateEntity(
                request,
                entity
        );

        SiteEntity updated =
                siteRepository.save(entity);

        return siteMapper.toResponse(updated);
    }

    @Override
    public void softDelete(Long id) {

        SiteEntity entity =
                siteRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found: " + id
                                )
                        );

        entity.setSiteStatus(StatusEnum.I);

        siteRepository.save(entity);
    }

    @Override
    public void hardDelete(Long id) {

        SiteEntity entity =
                siteRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Site not found: " + id
                                )
                        );

        siteRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> findActive() {

        return siteMapper.toResponseList(
                siteRepository.findBySiteStatus(
                        StatusEnum.A
                )
        );
    }

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}