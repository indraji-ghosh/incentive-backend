package org.example.incentivebackend.module.association.clientSite.service;

import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteRequest;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;
import org.example.incentivebackend.module.association.clientSite.entity.ClientSiteEntity;
import org.example.incentivebackend.module.association.clientSite.mapper.ClientSiteMapper;
import org.example.incentivebackend.module.association.clientSite.repository.ClientSiteRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientSiteServiceImplTest {

    @Mock
    private ClientSiteRepository clientSiteRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ClientSiteMapper clientSiteMapper;

    @InjectMocks
    private ClientSiteServiceImpl clientSiteService;

    @Test
    void assign_WithValidRequest_ShouldReturnResponse() {
        ClientSiteRequest request = new ClientSiteRequest();
        request.setClientId(1L);
        request.setSiteIds(List.of(2L));

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);

        SiteEntity site = new SiteEntity();
        site.setSiteId(2L);

        ClientSiteEntity savedEntity = new ClientSiteEntity();
        savedEntity.setClientSiteId(10L);
        savedEntity.setClient(client);
        savedEntity.setSite(site);

        ClientSiteResponse expectedResponse = ClientSiteResponse.builder()
                .clientSiteId(10L)
                .clientId(1L)
                .siteId(2L)
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findById(2L)).thenReturn(Optional.of(site));
        when(clientSiteRepository.existsByClientClientIdAndSiteSiteId(1L, 2L)).thenReturn(false);
        when(clientSiteRepository.saveAll(anyList())).thenReturn(List.of(savedEntity));
        when(clientSiteMapper.toResponseList(anyList())).thenReturn(List.of(expectedResponse));

        List<ClientSiteResponse> actualResponse = clientSiteService.assign(request);

        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.size());
        assertEquals(10L, actualResponse.get(0).getClientSiteId());
        verify(clientSiteRepository).saveAll(anyList());
    }

    @Test
    void assign_WithMissingClient_ShouldThrowException() {
        ClientSiteRequest request = new ClientSiteRequest();
        request.setClientId(1L);
        request.setSiteIds(List.of(2L));

        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientSiteService.assign(request));
        verify(clientSiteRepository, never()).saveAll(anyList());
    }

    @Test
    void assign_WithMissingSite_ShouldThrowException() {
        ClientSiteRequest request = new ClientSiteRequest();
        request.setClientId(1L);
        request.setSiteIds(List.of(2L));

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientSiteService.assign(request));
        verify(clientSiteRepository, never()).saveAll(anyList());
    }

    @Test
    void assign_WithDuplicateAssociation_ShouldThrowException() {
        ClientSiteRequest request = new ClientSiteRequest();
        request.setClientId(1L);
        request.setSiteIds(List.of(2L));

        ClientEntity client = new ClientEntity();
        client.setClientId(1L);

        SiteEntity site = new SiteEntity();
        site.setSiteId(2L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(siteRepository.findById(2L)).thenReturn(Optional.of(site));
        when(clientSiteRepository.existsByClientClientIdAndSiteSiteId(1L, 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> clientSiteService.assign(request));
        verify(clientSiteRepository, never()).saveAll(anyList());
    }

    @Test
    void findByClientId_WithExistingId_ShouldReturnList() {
        ClientSiteEntity entity = new ClientSiteEntity();
        List<ClientSiteEntity> entities = List.of(entity);
        ClientSiteResponse response = ClientSiteResponse.builder().build();
        List<ClientSiteResponse> responses = List.of(response);

        when(clientRepository.existsById(1L)).thenReturn(true);
        when(clientSiteRepository.findByClientClientId(1L)).thenReturn(entities);
        when(clientSiteMapper.toResponseList(entities)).thenReturn(responses);

        List<ClientSiteResponse> actualResponses = clientSiteService.findByClientId(1L);

        assertNotNull(actualResponses);
        assertEquals(1, actualResponses.size());
    }

    @Test
    void findByClientId_WithMissingId_ShouldThrowException() {
        when(clientRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> clientSiteService.findByClientId(1L));
    }

    @Test
    void findBySiteId_WithExistingId_ShouldReturnList() {
        ClientSiteEntity entity = new ClientSiteEntity();
        List<ClientSiteEntity> entities = List.of(entity);
        ClientSiteResponse response = ClientSiteResponse.builder().build();
        List<ClientSiteResponse> responses = List.of(response);

        when(siteRepository.existsById(2L)).thenReturn(true);
        when(clientSiteRepository.findBySiteSiteId(2L)).thenReturn(entities);
        when(clientSiteMapper.toResponseList(entities)).thenReturn(responses);

        List<ClientSiteResponse> actualResponses = clientSiteService.findBySiteId(2L);

        assertNotNull(actualResponses);
        assertEquals(1, actualResponses.size());
    }

    @Test
    void findBySiteId_WithMissingId_ShouldThrowException() {
        when(siteRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> clientSiteService.findBySiteId(2L));
    }

    @Test
    void remove_WithExistingAssociation_ShouldDeleteEntity() {
        ClientSiteEntity entity = new ClientSiteEntity();

        when(clientSiteRepository.findByClientClientIdAndSiteSiteId(1L, 2L)).thenReturn(Optional.of(entity));

        clientSiteService.remove(1L, 2L);

        verify(clientSiteRepository).delete(entity);
    }

    @Test
    void remove_WithMissingAssociation_ShouldThrowException() {
        when(clientSiteRepository.findByClientClientIdAndSiteSiteId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientSiteService.remove(1L, 2L));
        verify(clientSiteRepository, never()).delete(any());
    }
}
