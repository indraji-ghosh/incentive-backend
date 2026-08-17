package org.example.incentivebackend.module.master.paymenttype.service;

import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.mapper.PaymentTypeMapper;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
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
class PaymentTypeServiceImplTest {

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private PaymentTypeMapper paymentTypeMapper;

    @InjectMocks
    private PaymentTypeServiceImpl paymentTypeService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        PaymentTypeRequest request = new PaymentTypeRequest();
        request.setCode("TEST");

        PaymentTypeEntity entity = new PaymentTypeEntity();
        PaymentTypeEntity saved = new PaymentTypeEntity();
        PaymentTypeResponse expected = PaymentTypeResponse.builder().id(1L).build();

        when(paymentTypeRepository.existsByCodeIgnoreCase("TEST")).thenReturn(false);
        when(paymentTypeMapper.toEntity(request)).thenReturn(entity);
        when(paymentTypeRepository.save(entity)).thenReturn(saved);
        when(paymentTypeMapper.toResponse(saved)).thenReturn(expected);

        PaymentTypeResponse actual = paymentTypeService.create(request);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void create_WithDuplicateCode_ShouldThrowException() {
        PaymentTypeRequest request = new PaymentTypeRequest();
        request.setCode("TEST");

        when(paymentTypeRepository.existsByCodeIgnoreCase("TEST")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> paymentTypeService.create(request));
    }

    @Test
    void update_WithValidRequest_ShouldReturnResponse() {
        PaymentTypeRequest request = new PaymentTypeRequest();
        PaymentTypeEntity existing = new PaymentTypeEntity();
        PaymentTypeEntity saved = new PaymentTypeEntity();
        PaymentTypeResponse expected = PaymentTypeResponse.builder().id(1L).build();

        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(paymentTypeMapper).updateEntity(request, existing);
        when(paymentTypeRepository.save(existing)).thenReturn(saved);
        when(paymentTypeMapper.toResponse(saved)).thenReturn(expected);

        PaymentTypeResponse actual = paymentTypeService.update(1L, request);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        PaymentTypeEntity existing = new PaymentTypeEntity();
        PaymentTypeResponse expected = PaymentTypeResponse.builder().id(1L).build();

        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentTypeMapper.toResponse(existing)).thenReturn(expected);

        PaymentTypeResponse actual = paymentTypeService.findById(1L);

        assertNotNull(actual);
    }

    @Test
    void findAll_ShouldReturnPage() {
        PaymentTypeEntity entity = new PaymentTypeEntity();
        Page<PaymentTypeEntity> page = new PageImpl<>(List.of(entity));
        PaymentTypeResponse response = PaymentTypeResponse.builder().build();

        when(paymentTypeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(paymentTypeMapper.toResponse(entity)).thenReturn(response);

        Page<PaymentTypeResponse> actual = paymentTypeService.findAll("test", Pageable.unpaged());

        assertNotNull(actual);
        assertEquals(1, actual.getTotalElements());
    }

    @Test
    void delete_WithExistingId_ShouldDelete() {
        PaymentTypeEntity entity = new PaymentTypeEntity();
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(entity));

        paymentTypeService.delete(1L);

        verify(paymentTypeRepository).delete(entity);
    }

    @Test
    void getDropdown_ShouldReturnActiveItems() {
        PaymentTypeEntity e1 = new PaymentTypeEntity();
        e1.setId(1L);
        e1.setCode("C1");
        e1.setName("N1");
        e1.setAppStatus(StatusEnum.A.name());

        PaymentTypeEntity e2 = new PaymentTypeEntity();
        e2.setId(2L);
        e2.setAppStatus(StatusEnum.I.name());

        when(paymentTypeRepository.findAll()).thenReturn(List.of(e1, e2));

        List<DropdownDTO> actual = paymentTypeService.getDropdown();

        assertEquals(1, actual.size());
        assertEquals(1L, actual.get(0).getValue());
        assertEquals("[C1] N1", actual.get(0).getLabel());
    }
}
