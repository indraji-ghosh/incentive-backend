package org.example.incentivebackend.module.master.servicetype.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.DropdownDTO;
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
    public List<DropdownDTO> getDropdown() {
        return repository.findAll().stream()
                .map(entity -> new DropdownDTO(entity.getId(), entity.getName()))
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
