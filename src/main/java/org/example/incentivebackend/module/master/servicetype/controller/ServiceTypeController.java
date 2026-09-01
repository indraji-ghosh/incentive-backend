package org.example.incentivebackend.module.master.servicetype.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.servicetype.dto.request.ServiceTypeRequest;
import org.example.incentivebackend.module.master.servicetype.dto.response.ServiceTypeResponse;
import org.example.incentivebackend.module.master.servicetype.service.ServiceTypeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService service;

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceTypeResponse>> create(@Valid @RequestBody ServiceTypeRequest request) {
        return ResponseBuilder.created("ServiceType created", service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceTypeResponse>> update(@PathVariable Long id, @Valid @RequestBody ServiceTypeRequest request) {
        return ResponseBuilder.updated("ServiceType updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseBuilder.deleted("ServiceType deleted");
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceTypeResponse>> getById(@PathVariable Long id) {
        return ResponseBuilder.fetched("ServiceType fetched", service.getById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ServiceTypeResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        return ResponseBuilder.fetched("ServiceTypes fetched", service.getAll(page, size, search));
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> getDropdown() {
        return ResponseBuilder.fetched("ServiceType dropdown fetched", service.getDropdown());
    }
}
