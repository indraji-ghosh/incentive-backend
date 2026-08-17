package org.example.incentivebackend.module.master.sector.service;

import org.example.incentivebackend.module.master.sector.dto.SectorFilter;
import org.example.incentivebackend.module.master.sector.dto.SectorRequest;
import org.example.incentivebackend.module.master.sector.dto.SectorResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SectorService {

    SectorResponse create(SectorRequest request);

    Page<SectorResponse> findAll(SectorFilter filter);

    SectorResponse findById(Long id);

    SectorResponse update(
            Long id,
            SectorRequest request
    );

    void softDelete(Long id);

    void hardDelete(Long id);

    List<SectorResponse> findActive();
}