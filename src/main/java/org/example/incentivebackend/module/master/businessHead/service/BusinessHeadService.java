package org.example.incentivebackend.module.master.businessHead.service;

import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadRequest;
import org.example.incentivebackend.module.master.businessHead.dto.BusinessHeadResponse;

import java.util.List;

public interface BusinessHeadService {

    BusinessHeadResponse create(BusinessHeadRequest request);

    BusinessHeadResponse getById(Long id);

    List<BusinessHeadResponse> getAll();

    BusinessHeadResponse update(
            Long id,
            BusinessHeadRequest request
    );

    void delete(Long id);
}