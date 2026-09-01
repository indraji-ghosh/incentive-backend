package org.example.incentivebackend.module.transaction.rakeEntry.service;

import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;

import java.util.List;

public interface RakeEntryService {
    RakeEntryResponse create(RakeEntryRequest request);
    List<RakeEntryResponse> findAll();
    RakeEntryResponse findById(Long id);
    RakeEntryResponse update(Long id, RakeEntryRequest request);
    void softDelete(Long id);
    void hardDelete(Long id);
}
