package org.example.incentivebackend.module.master.servicetype.service;

import org.example.incentivebackend.module.master.servicetype.dto.request.ServiceTypeRequest;
import org.example.incentivebackend.module.master.servicetype.dto.response.ServiceTypeResponse;
import org.example.incentivebackend.common.DropdownDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ServiceTypeService {
    ServiceTypeResponse create(ServiceTypeRequest request);
    ServiceTypeResponse update(Long id, ServiceTypeRequest request);
    void delete(Long id);
    ServiceTypeResponse getById(Long id);
    Page<ServiceTypeResponse> getAll(int page, int size, String search);
    List<DropdownDTO> getDropdown();
}
