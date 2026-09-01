package org.example.incentivebackend.module.transaction.partyentry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.example.incentivebackend.module.transaction.partyentry.service.PartyEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction/party-entries")
@RequiredArgsConstructor
public class PartyEntryController {

    private final PartyEntryService partyEntryService;

    @PostMapping
    public ResponseEntity<ApiResponse<PartyEntryResponse>> create(@Valid @RequestBody PartyEntryRequest request) {
        PartyEntryResponse response = partyEntryService.create(request);
        return ResponseBuilder.created("Party Entry", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartyEntryResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<PartyEntryResponse> response = partyEntryService.findAll(search, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Party Entry", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyEntryResponse>> findById(@PathVariable Long id) {
        PartyEntryResponse response = partyEntryService.findById(id);
        return ResponseBuilder.fetched("Party Entry", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyEntryResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody PartyEntryRequest request) {
        PartyEntryResponse response = partyEntryService.update(id, request);
        return ResponseBuilder.updated("Party Entry", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        partyEntryService.delete(id);
        return ResponseBuilder.deleted("Party Entry");
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<java.util.List<org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyLookupResponse>>> getPartiesByClientId(@PathVariable Long clientId) {
        java.util.List<org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyLookupResponse> response = partyEntryService.findPartiesByClientId(clientId);
        return ResponseBuilder.list("Party Lookup", response);
    }
}
