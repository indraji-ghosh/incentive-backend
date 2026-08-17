package org.example.incentivebackend.module.master.unit.service;

import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.module.master.unit.dto.request.UnitCreateRequest;
import org.example.incentivebackend.module.master.unit.dto.request.UnitUpdateRequest;
import org.example.incentivebackend.module.master.unit.dto.response.UnitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UnitService {
    UnitResponse create(UnitCreateRequest request);
    UnitResponse update(Long id, UnitUpdateRequest request);
    UnitResponse findById(Long id);
    Page<UnitResponse> findAll(String search, Pageable pageable);
    void delete(Long id);
    List<DropdownDTO> getDropdown();
}
