package org.example.incentivebackend.module.master.client.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.client.dto.ClientFilter;
import org.example.incentivebackend.module.master.client.dto.ClientRequest;
import org.example.incentivebackend.module.master.client.dto.ClientResponse;
import org.example.incentivebackend.module.master.client.service.ClientService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master/clients")
public class ClientController {

    private final ClientService clientService;

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientRequest request
    ) {

        ClientResponse response =
                clientService.create(request);

        return ResponseBuilder.created(
                "Client",
                response
        );
    }

    // LIST
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> findAll(
            @ModelAttribute ClientFilter filter
    ) {

        Page<ClientResponse> response =
                clientService.findAll(filter);

        return ResponseBuilder.list(
                "Client",
                response
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> findById(
            @PathVariable Long id
    ) {

        ClientResponse response =
                clientService.findById(id);

        return ResponseBuilder.fetched(
                "Client",
                response
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request
    ) {

        ClientResponse response =
                clientService.update(id, request);

        return ResponseBuilder.updated(
                "Client",
                response
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(
            @PathVariable Long id
    ) {

        clientService.softDelete(id);

        return ResponseBuilder.deleted(
                "Client"
        );
    }

    // HARD DELETE
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<ApiResponse<Object>> hardDelete(
            @PathVariable Long id
    ) {

        clientService.hardDelete(id);

        return ResponseBuilder.deleted(
                "Client"
        );
    }

    // LOOKUP
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> lookup() {

        List<ClientResponse> clients =
                clientService.findActive();

        List<DropdownDTO> response =
                clients.stream()
                        .map(client ->
                                DropdownDTO.builder()
                                        .value(client.getClientId())
                                        .label(
                                                String.format(
                                                        "[%s] %s",
                                                        client.getClientShortCode(),
                                                        client.getClientName()
                                                )
                                        )
                                        .build()
                        )
                        .toList();

        return ResponseBuilder.fetched(
                "Client",
                response
        );
    }
}