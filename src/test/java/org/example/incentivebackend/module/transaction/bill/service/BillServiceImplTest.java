package org.example.incentivebackend.module.transaction.bill.service;

import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.transaction.bill.dto.BillAnnexureRequest;
import org.example.incentivebackend.module.transaction.bill.dto.request.BillRequest;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillResponse;
import org.example.incentivebackend.module.transaction.bill.entity.BillAnnexureEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PartyEntryRepository partyRepository;

    @InjectMocks
    private BillServiceImpl billService;

    @Test
    void create_WithValidRequest_ShouldReturnResponse() {
        BillRequest request = new BillRequest();
        request.setBillNumber("BILL-001");
        request.setClientId(1L);
        request.setPartyId(2L);
        request.setAnnexures(new ArrayList<>());

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);
        client.setClientName("Test Client");

        PartyEntryEntity party = new PartyEntryEntity();
        party.setId(2L);
        party.setPartyName("Test Party");

        BillEntity savedEntity = new BillEntity();
        savedEntity.setBillId(10L);
        savedEntity.setBillNumber("BILL-001");
        savedEntity.setClient(client);
        savedEntity.setParty(party);
        savedEntity.setAnnexures(new ArrayList<>());

        when(billRepository.existsByBillNumberIgnoreCase("BILL-001")).thenReturn(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));
        when(billRepository.save(any(BillEntity.class))).thenReturn(savedEntity);

        BillResponse response = billService.create(request);

        assertNotNull(response);
        assertEquals(10L, response.getBillId());
        assertEquals("BILL-001", response.getBillNumber());
        verify(billRepository).save(any(BillEntity.class));
    }

    @Test
    void create_WithDuplicateBillNumber_ShouldThrowException() {
        BillRequest request = new BillRequest();
        request.setBillNumber("BILL-001");

        when(billRepository.existsByBillNumberIgnoreCase("BILL-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> billService.create(request));
        verify(billRepository, never()).save(any(BillEntity.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnResponse() {
        ClientEntity client = new ClientEntity();
        client.setClientId(1L);

        PartyEntryEntity party = new PartyEntryEntity();
        party.setId(2L);

        BillEntity entity = new BillEntity();
        entity.setBillId(10L);
        entity.setClient(client);
        entity.setParty(party);
        entity.setAnnexures(new ArrayList<>());

        when(billRepository.findById(10L)).thenReturn(Optional.of(entity));

        BillResponse response = billService.findById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getBillId());
    }

    @Test
    void findById_WithMissingId_ShouldThrowException() {
        when(billRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.findById(10L));
    }

    @Test
    void update_WithValidRequest_ShouldReturnResponse() {
        BillRequest request = new BillRequest();
        request.setBillNumber("BILL-002");
        request.setClientId(1L);
        request.setPartyId(2L);
        request.setAnnexures(new ArrayList<>());

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);
        client.setClientName("Test Client");

        PartyEntryEntity party = new PartyEntryEntity();
        party.setId(2L);
        party.setPartyName("Test Party");

        BillEntity existingEntity = new BillEntity();
        existingEntity.setBillId(10L);
        existingEntity.setBillNumber("BILL-001");
        existingEntity.setClient(client);
        existingEntity.setParty(party);
        existingEntity.setAnnexures(new ArrayList<>());

        when(billRepository.findById(10L)).thenReturn(Optional.of(existingEntity));
        when(billRepository.existsByBillNumberIgnoreCase("BILL-002")).thenReturn(false);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(partyRepository.findById(2L)).thenReturn(Optional.of(party));

        BillResponse response = billService.update(10L, request);

        assertNotNull(response);
        assertEquals("BILL-002", existingEntity.getBillNumber());
        assertEquals("BILL-002", response.getBillNumber());
    }

    @Test
    void update_WithDuplicateBillNumber_ShouldThrowException() {
        BillRequest request = new BillRequest();
        request.setBillNumber("BILL-002");

        BillEntity existingEntity = new BillEntity();
        existingEntity.setBillId(10L);
        existingEntity.setBillNumber("BILL-001");

        when(billRepository.findById(10L)).thenReturn(Optional.of(existingEntity));
        when(billRepository.existsByBillNumberIgnoreCase("BILL-002")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> billService.update(10L, request));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteEntity() {
        BillEntity entity = new BillEntity();
        entity.setBillId(10L);

        when(billRepository.findById(10L)).thenReturn(Optional.of(entity));

        billService.delete(10L);

        verify(billRepository).delete(entity);
    }

    @Test
    void delete_WithMissingId_ShouldThrowException() {
        when(billRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.delete(10L));
        verify(billRepository, never()).delete(any(BillEntity.class));
    }
}
