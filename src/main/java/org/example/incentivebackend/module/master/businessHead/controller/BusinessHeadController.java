package org.example.incentivebackend.module.master.businessHead.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadRequest;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadResponse;
import org.example.incentivebackend.module.master.businessHead.service.BusinessHeadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/business-heads")
@RequiredArgsConstructor
public class BusinessHeadController {

    private final BusinessHeadService businessHeadService;

    @PostMapping
    public ResponseEntity<ApiResponse<BusinessHeadResponse>> create(
            @Valid @RequestBody BusinessHeadRequest request
    ) {

        BusinessHeadResponse response =
                businessHeadService.create(request);

        return ResponseBuilder.created(
                "Business Head",
                response
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessHeadResponse>> getById(
            @PathVariable Long id
    ) {

        BusinessHeadResponse response =
                businessHeadService.getById(id);

        return ResponseBuilder.fetched(
                "Business Head",
                response
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessHeadResponse>>> getAll() {

        List<BusinessHeadResponse> response =
                businessHeadService.getAll();

        return ResponseBuilder.list(
                "Business Head",
                response
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessHeadResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BusinessHeadRequest request
    ) {

        return ResponseEntity.ok(
                businessHeadService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(
            @PathVariable Long id
    ) {

        businessHeadService.delete(id);

        return ResponseBuilder.deleted("Business Head");
    }
}