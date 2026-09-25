package org.example.incentivebackend.module.association.partyassignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyAssignmentRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyServiceConfigurationRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyAssignmentResponse;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyServiceConfigurationResponse;
import org.example.incentivebackend.module.association.partyassignment.service.PartyAssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/association/party-assignments")
@RequiredArgsConstructor
public class PartyAssignmentController {

    private final PartyAssignmentService partyAssignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PartyAssignmentResponse>> create(@Valid @RequestBody PartyAssignmentRequest request) {
        PartyAssignmentResponse response = partyAssignmentService.create(request);
        return ResponseBuilder.created("Party Assignment", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartyAssignmentResponse>>> findAll(
            @RequestParam(required = false) Long partyId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long siteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<PartyAssignmentResponse> response = partyAssignmentService.findAll(partyId, clientId, siteId, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Party Assignments", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyAssignmentResponse>> findById(@PathVariable Long id) {
        PartyAssignmentResponse response = partyAssignmentService.findById(id);
        return ResponseBuilder.fetched("Party Assignment", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyAssignmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PartyAssignmentRequest request) {
        PartyAssignmentResponse response = partyAssignmentService.update(id, request);
        return ResponseBuilder.updated("Party Assignment", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        partyAssignmentService.delete(id);
        return ResponseBuilder.deleted("Party Assignment");
    }

    @GetMapping("/client/{clientId}/site/{siteId}")
    public ResponseEntity<ApiResponse<List<PartyAssignmentResponse>>> findByClientAndSite(
            @PathVariable Long clientId,
            @PathVariable Long siteId) {
        List<PartyAssignmentResponse> response = partyAssignmentService.findByClientAndSite(clientId, siteId);
        return ResponseBuilder.list("Party Assignments", response);
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<ApiResponse<List<PartyAssignmentResponse>>> findByParty(@PathVariable Long partyId) {
        List<PartyAssignmentResponse> response = partyAssignmentService.findByParty(partyId);
        return ResponseBuilder.list("Party Assignments", response);
    }

    @PostMapping("/{id}/configurations")
    public ResponseEntity<ApiResponse<PartyServiceConfigurationResponse>> addConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody PartyServiceConfigurationRequest request) {
        PartyServiceConfigurationResponse response = partyAssignmentService.addServiceConfiguration(id, request);
        return ResponseBuilder.created("Party Service Configuration", response);
    }

    @PutMapping("/{id}/configurations/{configId}")
    public ResponseEntity<ApiResponse<PartyServiceConfigurationResponse>> updateConfiguration(
            @PathVariable Long id,
            @PathVariable Long configId,
            @Valid @RequestBody PartyServiceConfigurationRequest request) {
        PartyServiceConfigurationResponse response = partyAssignmentService.updateServiceConfiguration(id, configId, request);
        return ResponseBuilder.updated("Party Service Configuration", response);
    }

    @DeleteMapping("/{id}/configurations/{configId}")
    public ResponseEntity<ApiResponse<Object>> deleteConfiguration(
            @PathVariable Long id,
            @PathVariable Long configId) {
        partyAssignmentService.removeServiceConfiguration(id, configId);
        return ResponseBuilder.deleted("Party Service Configuration");
    }
}
