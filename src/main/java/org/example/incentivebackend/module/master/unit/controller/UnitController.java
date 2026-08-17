package org.example.incentivebackend.module.master.unit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.example.incentivebackend.module.master.unit.service.UnitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @PostMapping
    public ResponseEntity<ApiResponse<UnitResponse>> create(@Valid @RequestBody UnitCreateRequest request) {
        UnitResponse response = unitService.create(request);
        return ResponseBuilder.created("Unit", response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UnitResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<UnitResponse> response = unitService.findAll(search, PageRequest.of(page, size, sort));
        return ResponseBuilder.list("Unit", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> findById(@PathVariable Long id) {
        UnitResponse response = unitService.findById(id);
        return ResponseBuilder.fetched("Unit", response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitResponse>> update(
            @PathVariable Long id, 
            @Valid @RequestBody UnitUpdateRequest request) {
        UnitResponse response = unitService.update(id, request);
        return ResponseBuilder.updated("Unit", response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseBuilder.deleted("Unit");
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> getDropdown() {
        List<DropdownDTO> dropdown = unitService.getDropdown();
        return ResponseBuilder.fetched("Unit", dropdown);
    }
}
