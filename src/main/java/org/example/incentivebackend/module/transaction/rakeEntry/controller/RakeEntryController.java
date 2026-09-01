package org.example.incentivebackend.module.transaction.rakeEntry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.service.RakeEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction/rake-entries")
public class RakeEntryController {

    private final RakeEntryService rakeEntryService;

    @PostMapping
    public ResponseEntity<ApiResponse<RakeEntryResponse>> create(
            @Valid @RequestBody RakeEntryRequest request
    ) {
        RakeEntryResponse response = rakeEntryService.create(request);
        return ResponseBuilder.created("Rake Entry", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RakeEntryResponse>>> findAll() {
        List<RakeEntryResponse> response = rakeEntryService.findAll();
        return ResponseBuilder.list("Rake Entries", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RakeEntryResponse>> findById(
            @PathVariable Long id
    ) {
        RakeEntryResponse response = rakeEntryService.findById(id);
        return ResponseBuilder.fetched("Rake Entry", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RakeEntryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RakeEntryRequest request
    ) {
        RakeEntryResponse response = rakeEntryService.update(id, request);
        return ResponseBuilder.updated("Rake Entry", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(
            @PathVariable Long id
    ) {
        rakeEntryService.softDelete(id);
        return ResponseBuilder.deleted("Rake Entry");
    }

    @DeleteMapping("/hard/{id}")
    public ResponseEntity<ApiResponse<Object>> hardDelete(
            @PathVariable Long id
    ) {
        rakeEntryService.hardDelete(id);
        return ResponseBuilder.deleted("Rake Entry permanently");
    }
}
