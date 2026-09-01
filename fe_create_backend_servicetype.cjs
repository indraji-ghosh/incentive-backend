const fs = require('fs');
const path = require('path');

const base = 'C:/Users/bmond/Documents/incentive-backend/src/main/java/org/example/incentivebackend/module/master/servicetype';

const entityCode = `package org.example.incentivebackend.module.master.servicetype.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.incentivebackend.common.entity.BaseEntity;

@Entity
@Table(name = "mst_service_type")
@Getter
@Setter
public class ServiceTypeEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
}
`;

const reqCode = `package org.example.incentivebackend.module.master.servicetype.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ServiceTypeRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
}
`;

const resCode = `package org.example.incentivebackend.module.master.servicetype.dto.response;

import lombok.Data;

@Data
public class ServiceTypeResponse {
    private Long id;
    private String name;
    private String description;
}
`;

const repoCode = `package org.example.incentivebackend.module.master.servicetype.repository;

import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {
}
`;

const serviceCode = `package org.example.incentivebackend.module.master.servicetype.service;

import org.example.incentivebackend.module.master.servicetype.dto.request.ServiceTypeRequest;
import org.example.incentivebackend.module.master.servicetype.dto.response.ServiceTypeResponse;
import org.example.incentivebackend.common.dto.DropdownOptionDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ServiceTypeService {
    ServiceTypeResponse create(ServiceTypeRequest request);
    ServiceTypeResponse update(Long id, ServiceTypeRequest request);
    void delete(Long id);
    ServiceTypeResponse getById(Long id);
    Page<ServiceTypeResponse> getAll(int page, int size, String search);
    List<DropdownOptionDTO> getDropdown();
}
`;

const serviceImplCode = `package org.example.incentivebackend.module.master.servicetype.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.dto.DropdownOptionDTO;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.servicetype.dto.request.ServiceTypeRequest;
import org.example.incentivebackend.module.master.servicetype.dto.response.ServiceTypeResponse;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTypeServiceImpl implements ServiceTypeService {

    private final ServiceTypeRepository repository;

    @Override
    public ServiceTypeResponse create(ServiceTypeRequest request) {
        ServiceTypeEntity entity = new ServiceTypeEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public ServiceTypeResponse update(Long id, ServiceTypeRequest request) {
        ServiceTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found with id " + id));
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public void delete(Long id) {
        ServiceTypeEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found with id " + id));
        repository.delete(entity);
    }

    @Override
    public ServiceTypeResponse getById(Long id) {
        return repository.findById(id).map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceType not found with id " + id));
    }

    @Override
    public Page<ServiceTypeResponse> getAll(int page, int size, String search) {
        return repository.findAll(PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Override
    public List<DropdownOptionDTO> getDropdown() {
        return repository.findAll().stream()
                .map(entity -> new DropdownOptionDTO(entity.getId(), entity.getName()))
                .collect(Collectors.toList());
    }

    private ServiceTypeResponse mapToResponse(ServiceTypeEntity entity) {
        ServiceTypeResponse response = new ServiceTypeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return response;
    }
}
`;

const controllerCode = `package org.example.incentivebackend.module.master.servicetype.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.dto.DropdownOptionDTO;
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
@RequestMapping("/api/v1/master/service-types")
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
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
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
    public ResponseEntity<ApiResponse<List<DropdownOptionDTO>>> getDropdown() {
        return ResponseBuilder.fetched("ServiceType dropdown fetched", service.getDropdown());
    }
}
`;

fs.writeFileSync(path.join(base, 'entity/ServiceTypeEntity.java'), entityCode);
fs.writeFileSync(path.join(base, 'dto/request/ServiceTypeRequest.java'), reqCode);
fs.writeFileSync(path.join(base, 'dto/response/ServiceTypeResponse.java'), resCode);
fs.writeFileSync(path.join(base, 'repository/ServiceTypeRepository.java'), repoCode);
fs.writeFileSync(path.join(base, 'service/ServiceTypeService.java'), serviceCode);
fs.writeFileSync(path.join(base, 'service/ServiceTypeServiceImpl.java'), serviceImplCode);
fs.writeFileSync(path.join(base, 'controller/ServiceTypeController.java'), controllerCode);

console.log("Created all ServiceType files");
