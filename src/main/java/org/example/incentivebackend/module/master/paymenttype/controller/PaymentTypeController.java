package org.example.incentivebackend.module.master.paymenttype.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.example.incentivebackend.module.master.paymenttype.service.PaymentTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/payment-types")
@RequiredArgsConstructor
public class PaymentTypeController {

    private final PaymentTypeService paymentTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentTypeResponse>> create(@Valid @RequestBody PaymentTypeRequest request) {
        PaymentTypeResponse response = paymentTypeService.create(request);
        return ResponseBuilder.created("Payment Type", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentTypeResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<PaymentTypeResponse> response = paymentTypeService.findAll(search, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Payment Type", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTypeResponse>> findById(@PathVariable Long id) {
        PaymentTypeResponse response = paymentTypeService.findById(id);
        return ResponseBuilder.fetched("Payment Type", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTypeResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody PaymentTypeRequest request) {
        PaymentTypeResponse response = paymentTypeService.update(id, request);
        return ResponseBuilder.updated("Payment Type", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        paymentTypeService.delete(id);
        return ResponseBuilder.deleted("Payment Type");
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> getDropdown() {
        List<DropdownDTO> dropdown = paymentTypeService.getDropdown();
        return ResponseBuilder.fetched("Payment Type", dropdown);
    }
}
