package org.example.incentivebackend.module.master.businessHead.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadRequest;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadResponse;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.businessHead.repository.BusinessHeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessHeadServiceTest {

    @Mock
    private BusinessHeadRepository businessHeadRepository;

    @InjectMocks
    private BusinessHeadServiceImpl businessHeadService;

    @Test
    void create_WithValidData_ShouldSaveAndReturnResponse() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");
        request.setHeadShortCode("FIN");
        request.setHeadStatus(StatusEnum.A);

        when(businessHeadRepository.existsByHeadShortCode("FIN")).thenReturn(false);

        BusinessHeadEntity savedEntity = new BusinessHeadEntity();
        savedEntity.setHeadId(1L);
        savedEntity.setHeadName("Finance");
        savedEntity.setHeadShortCode("FIN");
        savedEntity.setHeadStatus(StatusEnum.A);

        when(businessHeadRepository.save(any(BusinessHeadEntity.class))).thenReturn(savedEntity);

        BusinessHeadResponse response = businessHeadService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getHeadId());
        assertEquals("Finance", response.getHeadName());
        assertEquals("FIN", response.getHeadShortCode());
        assertEquals(StatusEnum.A, response.getHeadStatus());

        verify(businessHeadRepository).save(any(BusinessHeadEntity.class));
    }

    @Test
    void create_WithMissingStatus_ShouldDefaultToActive() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("Finance");
        request.setHeadShortCode("FIN");
        request.setHeadStatus(null);

        when(businessHeadRepository.existsByHeadShortCode("FIN")).thenReturn(false);

        BusinessHeadEntity savedEntity = new BusinessHeadEntity();
        savedEntity.setHeadId(1L);
        savedEntity.setHeadStatus(StatusEnum.A);

        when(businessHeadRepository.save(any(BusinessHeadEntity.class))).thenReturn(savedEntity);

        businessHeadService.create(request);

        ArgumentCaptor<BusinessHeadEntity> captor = ArgumentCaptor.forClass(BusinessHeadEntity.class);
        verify(businessHeadRepository).save(captor.capture());

        BusinessHeadEntity captured = captor.getValue();
        assertEquals(StatusEnum.A, captured.getHeadStatus());
    }

    @Test
    void create_WithDuplicateShortCode_ShouldThrowException() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadShortCode("FIN");

        when(businessHeadRepository.existsByHeadShortCode("FIN")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> businessHeadService.create(request));
        assertEquals("Business head short code already exists", exception.getMessage());

        verify(businessHeadRepository, never()).save(any());
    }

    @Test
    void getById_WithExistingId_ShouldReturnResponse() {
        BusinessHeadEntity entity = new BusinessHeadEntity();
        entity.setHeadId(1L);
        entity.setHeadName("Finance");

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(entity));

        BusinessHeadResponse response = businessHeadService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getHeadId());
        assertEquals("Finance", response.getHeadName());
    }

    @Test
    void getById_WithMissingId_ShouldThrowException() {
        when(businessHeadRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> businessHeadService.getById(1L));
        assertEquals("Business head not found: 1", exception.getMessage());
    }

    @Test
    void getAll_ShouldReturnList() {
        BusinessHeadEntity entity1 = new BusinessHeadEntity();
        entity1.setHeadId(1L);
        
        BusinessHeadEntity entity2 = new BusinessHeadEntity();
        entity2.setHeadId(2L);

        when(businessHeadRepository.findAll()).thenReturn(List.of(entity1, entity2));

        List<BusinessHeadResponse> response = businessHeadService.getAll();

        assertEquals(2, response.size());
    }

    @Test
    void getAll_WhenEmpty_ShouldReturnEmptyList() {
        when(businessHeadRepository.findAll()).thenReturn(List.of());

        List<BusinessHeadResponse> response = businessHeadService.getAll();

        assertTrue(response.isEmpty());
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnResponse() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("New Finance");
        request.setHeadShortCode("NFIN");

        BusinessHeadEntity existing = new BusinessHeadEntity();
        existing.setHeadId(1L);
        existing.setHeadName("Finance");
        existing.setHeadShortCode("FIN");

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessHeadRepository.existsByHeadShortCode("NFIN")).thenReturn(false);
        when(businessHeadRepository.save(any(BusinessHeadEntity.class))).thenReturn(existing);

        BusinessHeadResponse response = businessHeadService.update(1L, request);

        assertEquals("New Finance", response.getHeadName());
        assertEquals("NFIN", response.getHeadShortCode());
    }

    @Test
    void update_WithSameShortCode_ShouldNotCheckExistence() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadName("New Finance");
        request.setHeadShortCode("FIN"); // Same short code

        BusinessHeadEntity existing = new BusinessHeadEntity();
        existing.setHeadId(1L);
        existing.setHeadShortCode("FIN");

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessHeadRepository.save(any(BusinessHeadEntity.class))).thenReturn(existing);

        businessHeadService.update(1L, request);

        verify(businessHeadRepository, never()).existsByHeadShortCode(anyString());
    }

    @Test
    void update_WithDuplicateShortCode_ShouldThrowException() {
        BusinessHeadRequest request = new BusinessHeadRequest();
        request.setHeadShortCode("NFIN");

        BusinessHeadEntity existing = new BusinessHeadEntity();
        existing.setHeadId(1L);
        existing.setHeadShortCode("FIN");

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(businessHeadRepository.existsByHeadShortCode("NFIN")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> businessHeadService.update(1L, request));
        assertEquals("Business head short code already exists", exception.getMessage());
    }

    @Test
    void update_WithMissingId_ShouldThrowException() {
        BusinessHeadRequest request = new BusinessHeadRequest();

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> businessHeadService.update(1L, request));
        assertEquals("Business head not found: 1", exception.getMessage());
    }

    @Test
    void delete_WithExistingId_ShouldDelete() {
        BusinessHeadEntity existing = new BusinessHeadEntity();
        existing.setHeadId(1L);

        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(existing));

        businessHeadService.delete(1L);

        verify(businessHeadRepository).delete(existing);
    }

    @Test
    void delete_WithMissingId_ShouldThrowException() {
        when(businessHeadRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> businessHeadService.delete(1L));
        assertEquals("Business head not found: 1", exception.getMessage());

        verify(businessHeadRepository, never()).delete(any());
    }
}
