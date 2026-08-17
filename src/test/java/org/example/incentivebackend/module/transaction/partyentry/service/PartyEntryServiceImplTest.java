package org.example.incentivebackend.module.transaction.partyentry.service;

import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.example.incentivebackend.module.master.businessHead.repository.BusinessHeadRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyUnitConfigurationRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyUnitConfigurationEntity;
import org.example.incentivebackend.module.transaction.partyentry.mapper.PartyEntryMapper;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyEntryServiceImplTest {

    @Mock private PartyEntryRepository partyEntryRepository;
    @Mock private PartyEntryMapper partyEntryMapper;
    @Mock private BusinessHeadRepository businessHeadRepository;
    @Mock private PaymentTypeRepository paymentTypeRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private SiteRepository siteRepository;
    @Mock private UnitRepository unitRepository;

    @InjectMocks
    private PartyEntryServiceImpl partyEntryService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        PartyEntryRequest request = new PartyEntryRequest();
        request.setBusinessHeadId(1L);
        request.setPaymentTypeId(1L);
        request.setClientIds(List.of(1L));
        request.setSiteIds(List.of(1L));
        PartyUnitConfigurationRequest unitReq = new PartyUnitConfigurationRequest();
        unitReq.setUnitId(1L);
        unitReq.setRate(BigDecimal.TEN);
        request.setUnitConfigurations(List.of(unitReq));

        PartyEntryEntity entity = new PartyEntryEntity();
        PartyEntryResponse expected = PartyEntryResponse.builder().id(1L).build();

        when(partyEntryMapper.toEntity(request)).thenReturn(entity);
        when(businessHeadRepository.findById(1L)).thenReturn(Optional.of(new BusinessHeadEntity()));
        when(paymentTypeRepository.findById(1L)).thenReturn(Optional.of(new PaymentTypeEntity()));
        when(clientRepository.findAllById(List.of(1L))).thenReturn(List.of(new ClientEntity()));
        when(siteRepository.findAllById(List.of(1L))).thenReturn(List.of(new SiteEntity()));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(new UnitEntity()));
        when(partyEntryMapper.toEntity(unitReq)).thenReturn(new PartyUnitConfigurationEntity());
        when(partyEntryRepository.save(entity)).thenReturn(entity);
        when(partyEntryMapper.toResponse(entity)).thenReturn(expected);

        PartyEntryResponse actual = partyEntryService.create(request);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
    }

    @Test
    void create_WithMissingRelationships_ShouldThrowException() {
        PartyEntryRequest request = new PartyEntryRequest();
        request.setBusinessHeadId(1L);

        when(partyEntryMapper.toEntity(request)).thenReturn(new PartyEntryEntity());
        when(businessHeadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> partyEntryService.create(request));
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        PartyEntryEntity entity = new PartyEntryEntity();
        PartyEntryResponse expected = PartyEntryResponse.builder().id(1L).build();

        when(partyEntryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(partyEntryMapper.toResponse(entity)).thenReturn(expected);

        PartyEntryResponse actual = partyEntryService.findById(1L);

        assertNotNull(actual);
    }

    @Test
    void findAll_ShouldReturnPage() {
        Page<PartyEntryEntity> page = new PageImpl<>(List.of(new PartyEntryEntity()));
        when(partyEntryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(partyEntryMapper.toResponse(any(PartyEntryEntity.class))).thenReturn(PartyEntryResponse.builder().build());

        Page<PartyEntryResponse> actual = partyEntryService.findAll("test", Pageable.unpaged());

        assertEquals(1, actual.getTotalElements());
    }

    @Test
    void delete_ShouldDeleteEntity() {
        PartyEntryEntity entity = new PartyEntryEntity();
        when(partyEntryRepository.findById(1L)).thenReturn(Optional.of(entity));

        partyEntryService.delete(1L);

        verify(partyEntryRepository).delete(entity);
    }
}
