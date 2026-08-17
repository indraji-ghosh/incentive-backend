package org.example.incentivebackend.module.master.site.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.site.dto.SiteFilter;
import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.mapper.SiteMapper;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private SiteMapper siteMapper;

    @InjectMocks
    private SiteServiceImpl siteService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        SiteRequest request = new SiteRequest();
        request.setSiteShortCode("HQ");
        
        SiteEntity entity = new SiteEntity();
        entity.setSiteShortCode("HQ");
        
        SiteEntity savedEntity = new SiteEntity();
        savedEntity.setSiteId(1L);
        savedEntity.setSiteShortCode("HQ");
        savedEntity.setSiteStatus(StatusEnum.A);

        SiteResponse expectedResponse = SiteResponse.builder()
                .siteId(1L)
                .siteShortCode("HQ")
                .siteStatus(StatusEnum.A)
                .build();

        when(siteRepository.existsBySiteShortCodeIgnoreCase("HQ")).thenReturn(false);
        when(siteMapper.toEntity(request)).thenReturn(entity);
        when(siteRepository.save(entity)).thenReturn(savedEntity);
        when(siteMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        SiteResponse actualResponse = siteService.create(request);

        assertNotNull(actualResponse);
        assertEquals("HQ", actualResponse.getSiteShortCode());
        assertEquals(StatusEnum.A, entity.getSiteStatus());
        verify(siteRepository).save(entity);
    }

    @Test
    void create_WithDuplicateShortCode_ShouldThrowException() {
        SiteRequest request = new SiteRequest();
        request.setSiteShortCode("HQ");

        when(siteRepository.existsBySiteShortCodeIgnoreCase("HQ")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> siteService.create(request));
        verify(siteRepository, never()).save(any());
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        SiteEntity entity = new SiteEntity();
        entity.setSiteId(1L);

        SiteResponse expectedResponse = SiteResponse.builder()
                .siteId(1L)
                .build();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(siteMapper.toResponse(entity)).thenReturn(expectedResponse);

        SiteResponse actualResponse = siteService.findById(1L);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getSiteId());
    }

    @Test
    void findById_WithMissingId_ShouldThrowException() {
        when(siteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> siteService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnPage() {
        SiteFilter filter = new SiteFilter();
        filter.setPage(0);
        filter.setSize(10);
        filter.setSortBy("siteName");
        filter.setSortDirection("asc");
        filter.setSearch("test");
        filter.setState("state");

        SiteEntity entity = new SiteEntity();
        Page<SiteEntity> entityPage = new PageImpl<>(List.of(entity));

        SiteResponse response = SiteResponse.builder().build();

        when(siteRepository.findAll(eq("test"), eq("state"), any(), any(Pageable.class))).thenReturn(entityPage);
        when(siteMapper.toResponse(entity)).thenReturn(response);

        Page<SiteResponse> actualPage = siteService.findAll(filter);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
    }

    @Test
    void update_WithValidRequest_ShouldReturnResponse() {
        SiteRequest request = new SiteRequest();
        request.setSiteShortCode("HQ_UPD");

        SiteEntity existingEntity = new SiteEntity();
        existingEntity.setSiteId(1L);
        existingEntity.setSiteShortCode("HQ");

        SiteEntity savedEntity = new SiteEntity();
        savedEntity.setSiteId(1L);
        savedEntity.setSiteShortCode("HQ_UPD");

        SiteResponse expectedResponse = SiteResponse.builder()
                .siteId(1L)
                .siteShortCode("HQ_UPD")
                .build();

        when(siteRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(siteRepository.existsBySiteShortCodeIgnoreCaseAndSiteIdNot("HQ_UPD", 1L)).thenReturn(false);
        doNothing().when(siteMapper).updateEntity(request, existingEntity);
        when(siteRepository.save(existingEntity)).thenReturn(savedEntity);
        when(siteMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        SiteResponse actualResponse = siteService.update(1L, request);

        assertNotNull(actualResponse);
        assertEquals("HQ_UPD", actualResponse.getSiteShortCode());
        verify(siteRepository).save(existingEntity);
    }

    @Test
    void update_WithDuplicateShortCode_ShouldThrowException() {
        SiteRequest request = new SiteRequest();
        request.setSiteShortCode("HQ_DUP");

        SiteEntity existingEntity = new SiteEntity();
        existingEntity.setSiteId(1L);

        when(siteRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(siteRepository.existsBySiteShortCodeIgnoreCaseAndSiteIdNot("HQ_DUP", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> siteService.update(1L, request));
        verify(siteRepository, never()).save(any());
    }

    @Test
    void softDelete_ShouldSetStatusInactive() {
        SiteEntity entity = new SiteEntity();
        entity.setSiteId(1L);
        entity.setSiteStatus(StatusEnum.A);

        when(siteRepository.findById(1L)).thenReturn(Optional.of(entity));

        siteService.softDelete(1L);

        assertEquals(StatusEnum.I, entity.getSiteStatus());
        verify(siteRepository).save(entity);
    }

    @Test
    void hardDelete_ShouldDeleteFromDatabase() {
        SiteEntity entity = new SiteEntity();
        entity.setSiteId(1L);

        when(siteRepository.findById(1L)).thenReturn(Optional.of(entity));

        siteService.hardDelete(1L);

        verify(siteRepository).delete(entity);
    }

    @Test
    void findActive_ShouldReturnList() {
        SiteEntity entity = new SiteEntity();
        List<SiteEntity> entities = List.of(entity);
        SiteResponse response = SiteResponse.builder().build();
        List<SiteResponse> responses = List.of(response);

        when(siteRepository.findBySiteStatus(StatusEnum.A)).thenReturn(entities);
        when(siteMapper.toResponseList(entities)).thenReturn(responses);

        List<SiteResponse> actualResponses = siteService.findActive();

        assertNotNull(actualResponses);
        assertEquals(1, actualResponses.size());
    }
}
