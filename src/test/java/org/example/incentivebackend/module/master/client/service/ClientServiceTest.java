package org.example.incentivebackend.module.master.client.service;


import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.module.master.client.dto.ClientFilter;
import org.example.incentivebackend.module.master.client.dto.ClientRequest;
import org.example.incentivebackend.module.master.client.dto.ClientResponse;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.mapper.ClientMapper;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

        import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private ClientEntity clientEntity;
    private ClientRequest clientRequest;
    private ClientResponse clientResponse;

    @BeforeEach
    void setUp() {

        clientEntity = new ClientEntity();
        clientEntity.setClientId(1L);
        clientEntity.setClientName("ABC Limited");
        clientEntity.setClientShortCode("ABC");
        clientEntity.setClientStatus(StatusEnum.A);

        clientRequest = new ClientRequest();
        clientRequest.setClientName("ABC Limited");
        clientRequest.setClientShortCode("ABC");
        clientRequest.setClientStatus(StatusEnum.A);

        clientResponse = ClientResponse.builder()
                .clientId(1L)
                .clientName("ABC Limited")
                .clientShortCode("ABC")
                .clientStatus(StatusEnum.A)
                .build();
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void create_WithValidData_ShouldSaveAndReturnClient() {

        // Arrange
        when(clientRepository.existsByClientShortCode("ABC"))
                .thenReturn(false);

        when(clientMapper.toEntity(clientRequest))
                .thenReturn(clientEntity);

        when(clientRepository.save(clientEntity))
                .thenReturn(clientEntity);

        when(clientMapper.toResponse(clientEntity))
                .thenReturn(clientResponse);

        // Act
        ClientResponse result =
                clientService.create(clientRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("ABC Limited", result.getClientName());
        assertEquals("ABC", result.getClientShortCode());
        assertEquals(StatusEnum.A, result.getClientStatus());

        verify(clientRepository)
                .existsByClientShortCode("ABC");

        verify(clientRepository)
                .save(clientEntity);
    }

    @Test
    void create_WithDuplicateShortCode_ShouldThrowException() {

        // Arrange
        when(clientRepository.existsByClientShortCode("ABC"))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> clientService.create(clientRequest)
                );

        assertEquals(
                "Client short code already exists",
                exception.getMessage()
        );

        verify(clientRepository, never())
                .save(any(ClientEntity.class));

        verify(clientMapper, never())
                .toEntity(any(ClientRequest.class));
    }

    @Test
    void create_WithNullStatus_ShouldSetActive() {

        // Arrange
        clientRequest.setClientStatus(null);

        ClientEntity entity = new ClientEntity();
        entity.setClientName("ABC Limited");
        entity.setClientShortCode("ABC");
        entity.setClientStatus(null);

        when(clientRepository.existsByClientShortCode("ABC"))
                .thenReturn(false);

        when(clientMapper.toEntity(clientRequest))
                .thenReturn(entity);

        when(clientRepository.save(entity))
                .thenAnswer(invocation -> {

                    ClientEntity saved =
                            invocation.getArgument(0);

                    saved.setClientId(1L);

                    return saved;
                });

        when(clientMapper.toResponse(entity))
                .thenReturn(clientResponse);

        // Act
        clientService.create(clientRequest);

        // Assert
        assertEquals(
                StatusEnum.A,
                entity.getClientStatus()
        );

        verify(clientRepository).save(entity);
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void findById_WhenClientExists_ShouldReturnClient() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        when(clientMapper.toResponse(clientEntity))
                .thenReturn(clientResponse);

        // Act
        ClientResponse result =
                clientService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getClientId());
        assertEquals("ABC Limited", result.getClientName());

        verify(clientRepository)
                .findById(1L);

        verify(clientMapper)
                .toResponse(clientEntity);
    }

    @Test
    void findById_WhenClientDoesNotExist_ShouldThrowException() {

        // Arrange
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> clientService.findById(999L)
                );

        assertEquals(
                "Client not found: 999",
                exception.getMessage()
        );

        verify(clientMapper, never())
                .toResponse(any(ClientEntity.class));
    }

    // =========================================================
    // FIND ALL
    // =========================================================

    @Test
    void findAll_ShouldReturnClients() {

        // Arrange
        ClientFilter filter = new ClientFilter();

        filter.setPage(0);
        filter.setSize(10);
        filter.setSortBy("clientId");
        filter.setSortDirection("desc");

        Page<ClientEntity> page =
                new PageImpl<>(
                        List.of(clientEntity)
                );

        when(clientRepository.findAll(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        when(clientMapper.toResponse(clientEntity))
                .thenReturn(clientResponse);

        // Act
        Page<ClientResponse> result =
                clientService.findAll(filter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(
                "ABC Limited",
                result.getContent()
                        .get(0)
                        .getClientName()
        );

        verify(clientRepository)
                .findAll(
                        isNull(),
                        isNull(),
                        any(Pageable.class)
                );
    }

    @Test
    void findAll_WhenNoClients_ShouldReturnEmptyPage() {

        // Arrange
        ClientFilter filter = new ClientFilter();

        filter.setPage(0);
        filter.setSize(10);

        Page<ClientEntity> emptyPage =
                new PageImpl<>(List.of());

        when(clientRepository.findAll(
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        // Act
        Page<ClientResponse> result =
                clientService.findAll(filter);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void update_WithValidData_ShouldUpdateClient() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        when(clientRepository
                .existsByClientShortCodeAndClientIdNot(
                        "ABC",
                        1L
                ))
                .thenReturn(false);

        doNothing()
                .when(clientMapper)
                .updateEntity(
                        clientRequest,
                        clientEntity
                );

        when(clientRepository.save(clientEntity))
                .thenReturn(clientEntity);

        when(clientMapper.toResponse(clientEntity))
                .thenReturn(clientResponse);

        // Act
        ClientResponse result =
                clientService.update(
                        1L,
                        clientRequest
                );

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getClientId());

        verify(clientRepository)
                .findById(1L);

        verify(clientMapper)
                .updateEntity(
                        clientRequest,
                        clientEntity
                );

        verify(clientRepository)
                .save(clientEntity);
    }

    @Test
    void update_WithSameShortCode_ShouldAllowUpdate() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        when(clientRepository
                .existsByClientShortCodeAndClientIdNot(
                        "ABC",
                        1L
                ))
                .thenReturn(false);

        when(clientRepository.save(clientEntity))
                .thenReturn(clientEntity);

        when(clientMapper.toResponse(clientEntity))
                .thenReturn(clientResponse);

        // Act
        ClientResponse result =
                clientService.update(
                        1L,
                        clientRequest
                );

        // Assert
        assertNotNull(result);

        verify(clientRepository)
                .existsByClientShortCodeAndClientIdNot(
                        "ABC",
                        1L
                );

        verify(clientRepository)
                .save(clientEntity);
    }

    @Test
    void update_WithDuplicateShortCode_ShouldThrowException() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        when(clientRepository
                .existsByClientShortCodeAndClientIdNot(
                        "ABC",
                        1L
                ))
                .thenReturn(true);

        // Act + Assert
        DuplicateResourceException exception =
                assertThrows(
                        DuplicateResourceException.class,
                        () -> clientService.update(
                                1L,
                                clientRequest
                        )
                );

        assertEquals(
                "Client short code already exists",
                exception.getMessage()
        );

        verify(clientRepository, never())
                .save(any(ClientEntity.class));
    }

    @Test
    void update_WhenClientDoesNotExist_ShouldThrowException() {

        // Arrange
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> clientService.update(
                                999L,
                                clientRequest
                        )
                );

        assertEquals(
                "Client not found: 999",
                exception.getMessage()
        );

        verify(clientRepository, never())
                .save(any(ClientEntity.class));
    }

    // =========================================================
    // SOFT DELETE
    // =========================================================

    @Test
    void softDelete_ShouldSetStatusInactive() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        when(clientRepository.save(clientEntity))
                .thenReturn(clientEntity);

        // Act
        clientService.softDelete(1L);

        // Assert
        assertEquals(
                StatusEnum.I,
                clientEntity.getClientStatus()
        );

        verify(clientRepository)
                .save(clientEntity);
    }

    @Test
    void softDelete_WhenClientDoesNotExist_ShouldThrowException() {

        // Arrange
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> clientService.softDelete(999L)
                );

        assertEquals(
                "Client not found: 999",
                exception.getMessage()
        );

        verify(clientRepository, never())
                .save(any(ClientEntity.class));
    }

    // =========================================================
    // HARD DELETE
    // =========================================================

    @Test
    void hardDelete_ShouldDeleteClient() {

        // Arrange
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(clientEntity));

        // Act
        clientService.hardDelete(1L);

        // Assert
        verify(clientRepository)
                .delete(clientEntity);
    }

    @Test
    void hardDelete_WhenClientDoesNotExist_ShouldThrowException() {

        // Arrange
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> clientService.hardDelete(999L)
                );

        assertEquals(
                "Client not found: 999",
                exception.getMessage()
        );

        verify(clientRepository, never())
                .delete(any(ClientEntity.class));
    }

    // =========================================================
    // ACTIVE CLIENTS / LOOKUP
    // =========================================================

    @Test
    void findActive_ShouldReturnActiveClients() {

        // Arrange
        when(clientRepository.findByClientStatus(
                StatusEnum.A
        )).thenReturn(List.of(clientEntity));

        when(clientMapper.toResponseList(
                List.of(clientEntity)
        )).thenReturn(List.of(clientResponse));

        // Act
        List<ClientResponse> result =
                clientService.findActive();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(
                "ABC Limited",
                result.get(0).getClientName()
        );

        verify(clientRepository)
                .findByClientStatus(StatusEnum.A);
    }

    @Test
    void findActive_WhenNoActiveClients_ShouldReturnEmptyList() {

        // Arrange
        when(clientRepository.findByClientStatus(
                StatusEnum.A
        )).thenReturn(List.of());

        when(clientMapper.toResponseList(
                List.of()
        )).thenReturn(List.of());

        // Act
        List<ClientResponse> result =
                clientService.findActive();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
