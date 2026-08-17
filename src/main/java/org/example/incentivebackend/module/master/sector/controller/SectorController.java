package org.example.incentivebackend.module.master.sector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.sector.dto.SectorFilter;
import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.example.incentivebackend.module.master.sector.service.SectorService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master/sectors")
public class SectorController {

    private final SectorService sectorService;

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<SectorResponse>> create(
            @Valid @RequestBody SectorRequest request
    ) {

        SectorResponse response =
                sectorService.create(request);

        return ResponseBuilder.created(
                "Sector",
                response
        );
    }

    // LIST
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SectorResponse>>> findAll(
            @ModelAttribute SectorFilter filter
    ) {

        Page<SectorResponse> response =
                sectorService.findAll(filter);

        return ResponseBuilder.list(
                "Sector",
                response
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SectorResponse>> findById(
            @PathVariable Long id
    ) {

        SectorResponse response =
                sectorService.findById(id);

        return ResponseBuilder.fetched(
                "Sector",
                response
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SectorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SectorRequest request
    ) {

        SectorResponse response =
                sectorService.update(
                        id,
                        request
                );

        return ResponseBuilder.updated(
                "Sector",
                response
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(
            @PathVariable Long id
    ) {

        sectorService.softDelete(id);

        return ResponseBuilder.deleted(
                "Sector"
        );
    }

    // HARD DELETE
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<ApiResponse<Object>> hardDelete(
            @PathVariable Long id
    ) {

        sectorService.hardDelete(id);

        return ResponseBuilder.deleted(
                "Sector"
        );
    }

    // LOOKUP
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> lookup() {

        List<SectorResponse> sectors =
                sectorService.findActive();

        List<DropdownDTO> response =
                sectors.stream()
                        .map(sector ->
                                DropdownDTO.builder()
                                        .value(sector.getSectorId())
                                        .label(
                                                String.format(
                                                        "[%d] %s",
                                                        sector.getSectorId(),
                                                        sector.getSectorName()
                                                )
                                        )
                                        .build()
                        )
                        .toList();

        return ResponseBuilder.fetched(
                "Sector",
                response
        );
    }
}