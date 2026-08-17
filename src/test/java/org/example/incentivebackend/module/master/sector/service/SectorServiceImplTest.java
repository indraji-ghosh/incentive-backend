package org.example.incentivebackend.module.master.sector.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.sector.dto.SectorFilter;
import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.example.incentivebackend.module.master.sector.entity.SectorEntity;
import org.example.incentivebackend.module.master.sector.mapper.SectorMapper;
import org.example.incentivebackend.module.master.sector.repository.SectorRepository;
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
class SectorServiceImplTest {

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private SectorMapper sectorMapper;

    @InjectMocks
    private SectorServiceImpl sectorService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("IT");
        request.setSectorShortCode("IT_SEC");

        SectorEntity entity = new SectorEntity();
        entity.setSectorName("IT");
        entity.setSectorShortCode("IT_SEC");

        SectorEntity savedEntity = new SectorEntity();
        savedEntity.setSectorId(1L);
        savedEntity.setSectorName("IT");
        savedEntity.setSectorShortCode("IT_SEC");
        savedEntity.setSectorStatus(StatusEnum.A);

        SectorResponse expectedResponse = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("IT")
                .sectorShortCode("IT_SEC")
                .sectorStatus(StatusEnum.A)
                .build();

        when(sectorRepository.existsBySectorNameIgnoreCase("IT")).thenReturn(false);
        when(sectorMapper.toEntity(request)).thenReturn(entity);
        when(sectorRepository.save(entity)).thenReturn(savedEntity);
        when(sectorMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        SectorResponse actualResponse = sectorService.create(request);

        assertNotNull(actualResponse);
        assertEquals("IT", actualResponse.getSectorName());
        assertEquals(StatusEnum.A, entity.getSectorStatus()); // Verify default status was set
        verify(sectorRepository).save(entity);
    }

    @Test
    void create_WithDuplicateName_ShouldThrowException() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("IT");
        request.setSectorShortCode("IT_SEC");

        when(sectorRepository.existsBySectorNameIgnoreCase("IT")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> sectorService.create(request));
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void create_WithDuplicateShortCode_ShouldThrowException() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("IT");
        request.setSectorShortCode("IT_SEC");

        when(sectorRepository.existsBySectorNameIgnoreCase("IT")).thenReturn(false);
        when(sectorRepository.existsBySectorShortCodeIgnoreCase("IT_SEC")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> sectorService.create(request));
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        SectorEntity entity = new SectorEntity();
        entity.setSectorId(1L);
        entity.setSectorName("IT");

        SectorResponse expectedResponse = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("IT")
                .build();

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(sectorMapper.toResponse(entity)).thenReturn(expectedResponse);

        SectorResponse actualResponse = sectorService.findById(1L);

        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.getSectorId());
    }

    @Test
    void findById_WithMissingId_ShouldThrowException() {
        when(sectorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sectorService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnPage() {
        SectorFilter filter = new SectorFilter();
        filter.setPage(0);
        filter.setSize(10);
        filter.setSortBy("sectorName");
        filter.setSortDirection("asc");

        SectorEntity entity = new SectorEntity();
        Page<SectorEntity> entityPage = new PageImpl<>(List.of(entity));

        SectorResponse response = SectorResponse.builder().build();

        when(sectorRepository.findAll(any(), any(), any(Pageable.class))).thenReturn(entityPage);
        when(sectorMapper.toResponse(entity)).thenReturn(response);

        Page<SectorResponse> actualPage = sectorService.findAll(filter);

        assertNotNull(actualPage);
        assertEquals(1, actualPage.getTotalElements());
    }

    @Test
    void update_WithValidRequest_ShouldReturnResponse() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("Updated IT");
        request.setSectorShortCode("IT_UPD");

        SectorEntity existingEntity = new SectorEntity();
        existingEntity.setSectorId(1L);
        existingEntity.setSectorName("IT");
        existingEntity.setSectorShortCode("IT_SEC");

        SectorEntity savedEntity = new SectorEntity();
        savedEntity.setSectorId(1L);
        savedEntity.setSectorName("Updated IT");
        savedEntity.setSectorShortCode("IT_UPD");

        SectorResponse expectedResponse = SectorResponse.builder()
                .sectorId(1L)
                .sectorName("Updated IT")
                .sectorShortCode("IT_UPD")
                .build();

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(sectorRepository.existsBySectorNameIgnoreCaseAndSectorIdNot("Updated IT", 1L)).thenReturn(false);
        when(sectorRepository.existsBySectorShortCodeIgnoreCaseAndSectorIdNot("IT_UPD", 1L)).thenReturn(false);
        doNothing().when(sectorMapper).updateEntity(request, existingEntity);
        when(sectorRepository.save(existingEntity)).thenReturn(savedEntity);
        when(sectorMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        SectorResponse actualResponse = sectorService.update(1L, request);

        assertNotNull(actualResponse);
        assertEquals("Updated IT", actualResponse.getSectorName());
        assertEquals("IT_UPD", actualResponse.getSectorShortCode());
        verify(sectorRepository).save(existingEntity);
    }

    @Test
    void update_WithDuplicateName_ShouldThrowException() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("HR");
        request.setSectorShortCode("HR_SEC");

        SectorEntity existingEntity = new SectorEntity();
        existingEntity.setSectorId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(sectorRepository.existsBySectorNameIgnoreCaseAndSectorIdNot("HR", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> sectorService.update(1L, request));
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void update_WithDuplicateShortCode_ShouldThrowException() {
        SectorRequest request = new SectorRequest();
        request.setSectorName("HR");
        request.setSectorShortCode("HR_SEC");

        SectorEntity existingEntity = new SectorEntity();
        existingEntity.setSectorId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(sectorRepository.existsBySectorNameIgnoreCaseAndSectorIdNot("HR", 1L)).thenReturn(false);
        when(sectorRepository.existsBySectorShortCodeIgnoreCaseAndSectorIdNot("HR_SEC", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> sectorService.update(1L, request));
        verify(sectorRepository, never()).save(any());
    }

    @Test
    void softDelete_ShouldSetStatusInactive() {
        SectorEntity entity = new SectorEntity();
        entity.setSectorId(1L);
        entity.setSectorStatus(StatusEnum.A);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(entity));

        sectorService.softDelete(1L);

        assertEquals(StatusEnum.I, entity.getSectorStatus());
        verify(sectorRepository).save(entity);
    }

    @Test
    void hardDelete_ShouldDeleteFromDatabase() {
        SectorEntity entity = new SectorEntity();
        entity.setSectorId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(entity));

        sectorService.hardDelete(1L);

        verify(sectorRepository).delete(entity);
    }

    @Test
    void findActive_ShouldReturnList() {
        SectorEntity entity = new SectorEntity();
        List<SectorEntity> entities = List.of(entity);
        SectorResponse response = SectorResponse.builder().build();
        List<SectorResponse> responses = List.of(response);

        when(sectorRepository.findBySectorStatus(StatusEnum.A)).thenReturn(entities);
        when(sectorMapper.toResponseList(entities)).thenReturn(responses);

        List<SectorResponse> actualResponses = sectorService.findActive();

        assertNotNull(actualResponses);
        assertEquals(1, actualResponses.size());
    }
}
