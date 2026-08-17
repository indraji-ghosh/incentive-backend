package org.example.incentivebackend.module.master.site.service;

import org.example.incentivebackend.module.master.site.dto.SiteFilter;
import org.example.incentivebackend.module.master.site.dto.SiteRequest;
import org.example.incentivebackend.module.master.site.dto.SiteResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SiteService {

    SiteResponse create(SiteRequest request);

    Page<SiteResponse> findAll(SiteFilter filter);

    SiteResponse findById(Long id);

    SiteResponse update(
            Long id,
            SiteRequest request
    );

    void softDelete(Long id);

    void hardDelete(Long id);

    List<SiteResponse> findActive();
}