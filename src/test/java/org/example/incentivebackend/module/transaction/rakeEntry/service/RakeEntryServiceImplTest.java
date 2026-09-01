package org.example.incentivebackend.module.transaction.rakeEntry.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeAnnexureRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.mapper.RakeEntryMapper;
import org.example.incentivebackend.module.transaction.rakeEntry.repository.RakeEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RakeEntryServiceImplTest {

    @Mock
    private RakeEntryRepository rakeEntryRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PartyEntryRepository partyRepository;

    @Mock
    private RakeEntryMapper rakeEntryMapper;

    @InjectMocks
    private RakeEntryServiceImpl rakeEntryService;

    @Test
    void create_WithValidData_ShouldCreateRakeEntry() {
        RakeEntryRequest request = new RakeEntryRequest();
        request.setClientId(1L);
        request.setPartyId(2L);
        RakeAnnexureRequest annexureReq = new RakeAnnexureRequest();
        request.setAnnexures(List.of(annexureReq));

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);

        PartyEntryEntity party = new PartyEntryEntity();
        party.setId(2L);

        RakeEntryEntity entity = new RakeEntryEntity();
        entity.setAnnexures(new ArrayList<>());
        
        RakeAnnexureEntity annexureEntity = new RakeAnnexureEntity();

        RakeEntryResponse responseDto = RakeEntryResponse.builder().rakeEntryId(10L).build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));
        when(rakeEntryMapper.toEntity(request)).thenReturn(entity);
        when(rakeEntryMapper.toAnnexureEntity(annexureReq)).thenReturn(annexureEntity);
        when(rakeEntryRepository.save(any())).thenReturn(entity);
        when(rakeEntryMapper.toResponse(entity)).thenReturn(responseDto);

        RakeEntryResponse result = rakeEntryService.create(request);

        assertNotNull(result);
        assertEquals(10L, result.getRakeEntryId());
        assertEquals(StatusEnum.A, entity.getRakeStatus());
        assertEquals(client, entity.getClient());
        assertEquals(party, entity.getParty());
        assertEquals(1, entity.getAnnexures().size());
        assertEquals(entity, entity.getAnnexures().get(0).getRakeEntry());
        verify(rakeEntryRepository).save(entity);
    }

    @Test
    void create_WithInvalidClient_ShouldThrowNotFoundException() {
        RakeEntryRequest request = new RakeEntryRequest();
        request.setClientId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rakeEntryService.create(request));
        verify(rakeEntryRepository, never()).save(any());
    }

    @Test
    void create_WithInvalidParty_ShouldThrowNotFoundException() {
        RakeEntryRequest request = new RakeEntryRequest();
        request.setClientId(1L);
        request.setPartyId(2L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(new ClientEntity()));
        when(partyRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rakeEntryService.create(request));
        verify(rakeEntryRepository, never()).save(any());
    }

    @Test
    void findById_WhenExists_ShouldReturnRakeEntry() {
        RakeEntryEntity entity = new RakeEntryEntity();
        RakeEntryResponse responseDto = RakeEntryResponse.builder().rakeEntryId(10L).build();

        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(rakeEntryMapper.toResponse(entity)).thenReturn(responseDto);

        RakeEntryResponse result = rakeEntryService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getRakeEntryId());
    }

    @Test
    void findById_WhenNotExists_ShouldThrowNotFoundException() {
        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rakeEntryService.findById(10L));
    }

    @Test
    void update_WithValidData_ShouldUpdateRakeEntry() {
        RakeEntryRequest request = new RakeEntryRequest();
        request.setClientId(1L);
        request.setPartyId(2L);
        RakeAnnexureRequest annexureReq = new RakeAnnexureRequest();
        request.setAnnexures(List.of(annexureReq));

        RakeEntryEntity existingEntity = new RakeEntryEntity();
        existingEntity.setAnnexures(new ArrayList<>());
        
        ClientEntity client = new ClientEntity();
        PartyEntryEntity party = new PartyEntryEntity();
        RakeAnnexureEntity annexureEntity = new RakeAnnexureEntity();

        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.of(existingEntity));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));
        when(rakeEntryMapper.toAnnexureEntity(annexureReq)).thenReturn(annexureEntity);
        when(rakeEntryMapper.toResponse(existingEntity)).thenReturn(RakeEntryResponse.builder().rakeEntryId(10L).build());

        RakeEntryResponse result = rakeEntryService.update(10L, request);

        assertNotNull(result);
        verify(rakeEntryMapper).updateEntityFromRequest(request, existingEntity);
        assertEquals(client, existingEntity.getClient());
        assertEquals(party, existingEntity.getParty());
        assertEquals(1, existingEntity.getAnnexures().size());
        assertEquals(existingEntity, existingEntity.getAnnexures().get(0).getRakeEntry());
    }

    @Test
    void update_WhenNotFound_ShouldThrowNotFoundException() {
        RakeEntryRequest request = new RakeEntryRequest();
        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rakeEntryService.update(10L, request));
    }

    @Test
    void softDelete_WhenExists_ShouldUpdateStatus() {
        RakeEntryEntity entity = new RakeEntryEntity();
        entity.setRakeStatus(StatusEnum.A);
        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.of(entity));

        rakeEntryService.softDelete(10L);

        assertEquals(StatusEnum.I, entity.getRakeStatus());
    }

    @Test
    void hardDelete_WhenExists_ShouldDeleteEntity() {
        RakeEntryEntity entity = new RakeEntryEntity();
        when(rakeEntryRepository.findById(10L)).thenReturn(Optional.of(entity));

        rakeEntryService.hardDelete(10L);

        verify(rakeEntryRepository).delete(entity);
    }
}
