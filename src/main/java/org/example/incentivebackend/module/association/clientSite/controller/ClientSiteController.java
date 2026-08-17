package org.example.incentivebackend.module.association.clientSite.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteRequest;
import org.example.incentivebackend.module.association.clientSite.dto.ClientSiteResponse;
import org.example.incentivebackend.module.association.clientSite.service.ClientSiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/association/client-sites")
public class ClientSiteController {

    private final ClientSiteService clientSiteService;

    /**
     * Assign site to client
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<ClientSiteResponse>>> assign(
            @Valid @RequestBody ClientSiteRequest request
    ) {

        List<ClientSiteResponse> response =
                clientSiteService.assign(request);

        return ResponseBuilder.created(
                "Client Sites",
                response
        );
    }

    /**
     * Get all sites of a client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<
            ApiResponse<List<ClientSiteResponse>>
            > getByClient(
            @PathVariable Long clientId
    ) {

        List<ClientSiteResponse> response =
                clientSiteService.findByClientId(
                        clientId
                );

        return ResponseBuilder.list(
                "Client Site",
                response
        );
    }

    /**
     * Get client assigned to a site
     */
    @GetMapping("/site/{siteId}")
    public ResponseEntity<ApiResponse<List<ClientSiteResponse>>> getBySite(
            @PathVariable Long siteId
    ) {

        List<ClientSiteResponse> response =
                clientSiteService.findBySiteId(siteId);

        return ResponseBuilder.list(
                "Client Site",
                response
        );
    }

    /**
     * Remove site from client
     */
    @DeleteMapping(
            "/client/{clientId}/site/{siteId}"
    )
    public ResponseEntity<ApiResponse<Object>> remove(
            @PathVariable Long clientId,
            @PathVariable Long siteId
    ) {

        clientSiteService.remove(
                clientId,
                siteId
        );

        return ResponseBuilder.deleted(
                "Client Site"
        );
    }
}