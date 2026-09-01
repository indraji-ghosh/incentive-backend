package org.example.incentivebackend.module.transaction.bill.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.transaction.bill.dto.request.BillRequest;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillResponse;
import org.example.incentivebackend.module.transaction.bill.service.BillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction/bill-entries")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> create(@Valid @RequestBody BillRequest request) {
        BillResponse response = billService.create(request);
        return ResponseBuilder.created("Bill Entry", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BillResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "billId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<BillResponse> response = billService.findAll(search, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Bill Entry", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> findById(@PathVariable Long id) {
        BillResponse response = billService.findById(id);
        return ResponseBuilder.fetched("Bill Entry", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody BillRequest request) {
        BillResponse response = billService.update(id, request);
        return ResponseBuilder.updated("Bill Entry", response);
    }

    @DeleteMapping("/hard/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        billService.delete(id);
        return ResponseBuilder.deleted("Bill Entry");
    }
}
