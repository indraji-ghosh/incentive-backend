package org.example.incentivebackend.common.audit.controller;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.audit.dto.response.AuditLogResponse;
import org.example.incentivebackend.common.audit.entity.AuditLogEntity;
import org.example.incentivebackend.common.audit.enums.AuditAction;
import org.example.incentivebackend.common.audit.mapper.AuditLogMapper;
import org.example.incentivebackend.common.audit.service.AuditLogService;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.common.response.ApiResponse;
import org.example.incentivebackend.common.response.ResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogMapper auditLogMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "performedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long performedBy,
            @RequestParam(required = false) String businessReference) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Page<AuditLogEntity> entities = auditLogService.findAll(
                moduleName, entityName, entityId, action, performedBy, businessReference,
                PageRequest.of(page, size, sort)
        );

        Page<AuditLogResponse> response = entities.map(auditLogMapper::toResponse);
        return ResponseBuilder.list("Audit Logs", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> findById(@PathVariable Long id) {
        AuditLogEntity entity = auditLogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit Log not found with id: " + id));
        return ResponseBuilder.fetched("Audit Log", auditLogMapper.toResponse(entity));
    }
}
