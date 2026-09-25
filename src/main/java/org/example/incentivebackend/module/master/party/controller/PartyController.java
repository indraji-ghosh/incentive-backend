package org.example.incentivebackend.module.master.party.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.party.dto.request.PartyRequest;
import org.example.incentivebackend.module.master.party.dto.response.PartyResponse;
import org.example.incentivebackend.module.master.party.service.PartyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    public ResponseEntity<ApiResponse<PartyResponse>> create(@Valid @RequestBody PartyRequest request) {
        PartyResponse response = partyService.create(request);
        return ResponseBuilder.created("Party", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PartyResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<PartyResponse> response = partyService.findAll(search, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Party", response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> findAllActive() {
        List<PartyResponse> response = partyService.findAllActive();
        return ResponseBuilder.list("Active Parties", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyResponse>> findById(@PathVariable Long id) {
        PartyResponse response = partyService.findById(id);
        return ResponseBuilder.fetched("Party", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody PartyRequest request) {
        PartyResponse response = partyService.update(id, request);
        return ResponseBuilder.updated("Party", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        partyService.delete(id);
        return ResponseBuilder.deleted("Party");
    }
}
