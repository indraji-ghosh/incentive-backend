package org.example.incentivebackend.module.master.unit.service;

import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.mapper.UnitMapper;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitMapper unitMapper;

    @InjectMocks
    private UnitServiceImpl unitService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        UnitCreateRequest request = new UnitCreateRequest();
        request.setCode("TEST");

        UnitEntity entity = new UnitEntity();
        UnitEntity saved = new UnitEntity();
        UnitResponse expected = UnitResponse.builder().id(1L).build();

        when(unitRepository.existsByCodeIgnoreCase("TEST")).thenReturn(false);
        when(unitMapper.toEntity(request)).thenReturn(entity);
        when(unitRepository.save(entity)).thenReturn(saved);
        when(unitMapper.toResponse(saved)).thenReturn(expected);

        UnitResponse actual = unitService.create(request);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowException() {
        UnitCreateRequest request = new UnitCreateRequest();
        request.setCode("TEST");

        when(unitRepository.existsByCodeIgnoreCase("TEST")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> unitService.create(request));
    }

    @Test
    void update_WithValidRequest_ShouldReturnResponse() {
        UnitUpdateRequest request = new UnitUpdateRequest();
        UnitEntity existing = new UnitEntity();
        UnitEntity saved = new UnitEntity();
        UnitResponse expected = UnitResponse.builder().id(1L).build();

        when(unitRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(unitMapper).updateEntity(request, existing);
        when(unitRepository.save(existing)).thenReturn(saved);
        when(unitMapper.toResponse(saved)).thenReturn(expected);

        UnitResponse actual = unitService.update(1L, request);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        UnitEntity existing = new UnitEntity();
        UnitResponse expected = UnitResponse.builder().id(1L).build();

        when(unitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(unitMapper.toResponse(existing)).thenReturn(expected);

        UnitResponse actual = unitService.findById(1L);

        assertNotNull(actual);
    }

    @Test
    void findAll_ShouldReturnPage() {
        UnitEntity entity = new UnitEntity();
        Page<UnitEntity> page = new PageImpl<>(List.of(entity));
        UnitResponse response = UnitResponse.builder().build();

        when(unitRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(unitMapper.toResponse(entity)).thenReturn(response);

        Page<UnitResponse> actual = unitService.findAll("test", Pageable.unpaged());

        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
    }

    @Test
    void delete_WithExistingId_ShouldDelete() {
        UnitEntity entity = new UnitEntity();
        when(unitRepository.findById(1L)).thenReturn(Optional.of(entity));

        unitService.delete(1L);

        verify(unitRepository).delete(entity);
    }

    @Test
    void getDropdown_ShouldReturnActiveItems() {
        UnitEntity e1 = new UnitEntity();
        e1.setId(1L);
        e1.setCode("C1");
        e1.setName("N1");

        when(unitRepository.findAll()).thenReturn(List.of(e1));

        List<DropdownDTO> actual = unitService.getDropdown();

        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getValue());
        assertEquals("N1", actual.get(0).getLabel());
    }
}
