package org.example.incentivebackend.module.master.client.service;


import org.example.incentivebackend.module.master.client.dto.ClientFilter;
import org.example.incentivebackend.module.master.client.dto.ClientRequest;
import org.example.incentivebackend.module.master.client.dto.ClientResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ClientService {

    ClientResponse create(ClientRequest request);

    Page<ClientResponse> findAll(ClientFilter filter);

    ClientResponse findById(Long id);

    ClientResponse update(
            Long id,
            ClientRequest request
    );

    void softDelete(Long id);

    void hardDelete(Long id);

    List<ClientResponse> findActive();
}
