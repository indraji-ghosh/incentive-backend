    package org.example.incentivebackend.module.master.client.service;

    import lombok.RequiredArgsConstructor;
    import org.example.incentivebackend.common.enums.StatusEnum;
    import org.example.incentivebackend.common.exception.DuplicateResourceException;
    import org.example.incentivebackend.common.exception.ResourceNotFoundException;
    import org.example.incentivebackend.module.master.client.dto.ClientFilter;
    import org.example.incentivebackend.module.master.client.dto.ClientRequest;
    import org.example.incentivebackend.module.master.client.dto.ClientResponse;
    import org.example.incentivebackend.module.master.client.entity.ClientEntity;
    import org.example.incentivebackend.module.master.client.mapper.ClientMapper;
    import org.example.incentivebackend.module.master.client.repository.ClientRepository;
    import org.springframework.data.domain.*;
            import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.lang.module.ResolutionException;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    @Transactional
    public class ClientServiceImpl implements ClientService {

        private final ClientRepository clientRepository;
        private final ClientMapper clientMapper;
        private final org.example.incentivebackend.common.audit.service.AuditLogService auditLogService;
        private final org.example.incentivebackend.common.audit.util.AuditHelper auditHelper;

        @Override
        public ClientResponse create(ClientRequest request) {

            if (clientRepository.existsByClientShortCode(
                    request.getClientShortCode()
            )) {
                throw new DuplicateResourceException(
                        "Client short code already exists"
                );
            }

            ClientEntity entity =
                    clientMapper.toEntity(request);

            if (entity.getClientStatus() == null) {
                entity.setClientStatus(StatusEnum.A);
            }

            ClientEntity saved =
                    clientRepository.save(entity);

            ClientResponse response = clientMapper.toResponse(saved);
            
            auditLogService.createAuditLog(
                "MASTER", "Client", "ms_client", saved.getClientId(),
                org.example.incentivebackend.common.audit.enums.AuditAction.CREATE,
                null, auditHelper.toJson(response),
                "Client created", 1L,
                saved.getClientShortCode(), null, null, null, "SUCCESS"
            );

            return response;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ClientResponse> findAll(
                ClientFilter filter
        ) {

            Sort.Direction direction =
                    "asc".equalsIgnoreCase(
                            filter.getSortDirection()
                    )
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;

            Sort sort = Sort.by(
                    direction,
                    filter.getSortBy()
            );

            Pageable pageable =
                    PageRequest.of(
                            filter.getPage(),
                            filter.getSize(),
                            sort
                    );

            String search = filter.getSearch();

            if (search != null && search.isBlank()) {
                search = null;
            }

            Page<ClientEntity> clients =
                    clientRepository.findAll(
                            search,
                            filter.getClientStatus(),
                            pageable
                    );

            return clients.map(clientMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public ClientResponse findById(Long id) {

            ClientEntity entity =
                    clientRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Client not found: " + id
                                    )
                            );

            return clientMapper.toResponse(entity);
        }

        @Override
        public ClientResponse update(
                Long id,
                ClientRequest request
        ) {

            ClientEntity entity =
                    clientRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Client not found: " + id
                                    )
                            );
                            
            String oldStateJson = auditHelper.toJson(clientMapper.toResponse(entity));

            if (clientRepository
                    .existsByClientShortCodeAndClientIdNot(
                            request.getClientShortCode(),
                            id
                    )) {

                throw new DuplicateResourceException(
                        "Client short code already exists"
                );
            }

            clientMapper.updateEntity(
                    request,
                    entity
            );

            ClientEntity updated =
                    clientRepository.save(entity);

            ClientResponse response = clientMapper.toResponse(updated);
            
            auditLogService.createAuditLog(
                "MASTER", "Client", "ms_client", updated.getClientId(),
                org.example.incentivebackend.common.audit.enums.AuditAction.UPDATE,
                oldStateJson, auditHelper.toJson(response),
                "Client updated", 1L,
                updated.getClientShortCode(), null, null, null, "SUCCESS"
            );

            return response;
        }

        @Override
        public void softDelete(Long id) {

            ClientEntity entity =
                    clientRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Client not found: " + id
                                    )
                            );

            String oldStateJson = auditHelper.toJson(clientMapper.toResponse(entity));
            entity.setClientStatus(StatusEnum.I);

            ClientEntity saved = clientRepository.save(entity);
            
            auditLogService.createAuditLog(
                "MASTER", "Client", "ms_client", id,
                org.example.incentivebackend.common.audit.enums.AuditAction.SOFT_DELETE,
                oldStateJson, auditHelper.toJson(clientMapper.toResponse(saved)),
                "Client soft deleted", 1L,
                saved.getClientShortCode(), null, null, null, "SUCCESS"
            );
        }

        @Override
        public void hardDelete(Long id) {

            ClientEntity entity =
                    clientRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Client not found: " + id
                                    )
                            );

            String oldStateJson = auditHelper.toJson(clientMapper.toResponse(entity));
            clientRepository.delete(entity);
            
            auditLogService.createAuditLog(
                "MASTER", "Client", "ms_client", id,
                org.example.incentivebackend.common.audit.enums.AuditAction.DELETE,
                oldStateJson, null,
                "Client hard deleted", 1L,
                entity.getClientShortCode(), null, null, null, "SUCCESS"
            );
        }

        @Override
        @Transactional(readOnly = true)
        public List<ClientResponse> findActive() {

            return clientMapper.toResponseList(
                    clientRepository.findByClientStatus(
                            StatusEnum.A
                    )
            );
        }
    }
