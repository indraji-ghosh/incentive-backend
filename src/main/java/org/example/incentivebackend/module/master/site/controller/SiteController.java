package org.example.incentivebackend.module.master.site.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.site.dto.SiteFilter;
import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.example.incentivebackend.module.master.site.service.SiteService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/master/sites")
public class SiteController {

    private final SiteService siteService;

    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<SiteResponse>> create(
            @Valid @RequestBody SiteRequest request
    ) {

        SiteResponse response =
                siteService.create(request);

        return ResponseBuilder.created(
                "Site",
                response
        );
    }

    // LIST
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SiteResponse>>> findAll(
            @ModelAttribute SiteFilter filter
    ) {

        Page<SiteResponse> response =
                siteService.findAll(filter);

        return ResponseBuilder.list(
                "Site",
                response
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> findById(
            @PathVariable Long id
    ) {

        SiteResponse response =
                siteService.findById(id);

        return ResponseBuilder.fetched(
                "Site",
                response
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request
    ) {

        SiteResponse response =
                siteService.update(
                        id,
                        request
                );

        return ResponseBuilder.updated(
                "Site",
                response
        );
    }

    // SOFT DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> softDelete(
            @PathVariable Long id
    ) {

        siteService.softDelete(id);

        return ResponseBuilder.deleted(
                "Site"
        );
    }

    // HARD DELETE
    @DeleteMapping("/hard/{id}")
    public ResponseEntity<ApiResponse<Object>> hardDelete(
            @PathVariable Long id
    ) {

        siteService.hardDelete(id);

        return ResponseBuilder.deleted(
                "Site"
        );
    }

    // LOOKUP
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<DropdownDTO>>> lookup() {

        List<SiteResponse> sites =
                siteService.findActive();

        List<DropdownDTO> response =
                sites.stream()
                        .map(site -> {

                            String shortCode =
                                    site.getSiteShortCode() != null
                                            ? site.getSiteShortCode().trim()
                                            : "";

                            String name =
                                    site.getSiteName() != null
                                            ? site.getSiteName().trim()
                                            : "";

                            String label;

                            if (!shortCode.isEmpty()
                                    && !name.isEmpty()) {

                                label = String.format(
                                        "[%s] %s",
                                        shortCode,
                                        name
                                );

                            } else if (!shortCode.isEmpty()) {

                                label = String.format(
                                        "[%s]",
                                        shortCode
                                );

                            } else {

                                label = name;
                            }

                            return DropdownDTO.builder()
                                    .value(site.getSiteId())
                                    .label(label)
                                    .build();
                        })
                        .toList();

        return ResponseBuilder.fetched(
                "Site",
                response
        );
    }
}