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
    private final org.example.incentivebackend.common.audit.service.AuditLogService auditLogService;
    private final org.example.incentivebackend.common.audit.util.AuditHelper auditHelper;

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

        SiteResponse response = siteMapper.toResponse(saved);
        
        auditLogService.createAuditLog(
            "MASTER", "Site", "ms_site", saved.getSiteId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.CREATE,
            null, auditHelper.toJson(response),
            "Site created", 1L,
            saved.getSiteShortCode(), null, null, null, "SUCCESS"
        );

        return response;
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
                        
        String oldStateJson = auditHelper.toJson(siteMapper.toResponse(entity));

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

        SiteResponse response = siteMapper.toResponse(updated);
        
        auditLogService.createAuditLog(
            "MASTER", "Site", "ms_site", updated.getSiteId(),
            org.example.incentivebackend.common.audit.enums.AuditAction.UPDATE,
            oldStateJson, auditHelper.toJson(response),
            "Site updated", 1L,
            updated.getSiteShortCode(), null, null, null, "SUCCESS"
        );

        return response;
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

        String oldStateJson = auditHelper.toJson(siteMapper.toResponse(entity));
        entity.setSiteStatus(StatusEnum.I);

        SiteEntity saved = siteRepository.save(entity);
        
        auditLogService.createAuditLog(
            "MASTER", "Site", "ms_site", id,
            org.example.incentivebackend.common.audit.enums.AuditAction.SOFT_DELETE,
            oldStateJson, auditHelper.toJson(siteMapper.toResponse(saved)),
            "Site soft deleted", 1L,
            saved.getSiteShortCode(), null, null, null, "SUCCESS"
        );
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

        String oldStateJson = auditHelper.toJson(siteMapper.toResponse(entity));
        siteRepository.delete(entity);
        
        auditLogService.createAuditLog(
            "MASTER", "Site", "ms_site", id,
            org.example.incentivebackend.common.audit.enums.AuditAction.DELETE,
            oldStateJson, null,
            "Site hard deleted", 1L,
            entity.getSiteShortCode(), null, null, null, "SUCCESS"
        );
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