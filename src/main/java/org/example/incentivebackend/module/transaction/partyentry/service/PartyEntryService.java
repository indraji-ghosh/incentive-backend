package org.example.incentivebackend.module.transaction.partyentry.service;

import org.example.incentivebackend.module.transaction.partyentry.dto.request.PartyEntryRequest;
import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.example.incentivebackend.module.transaction.partyentry.dto.response.PartyLookupResponse;
import java.util.List;

public interface PartyEntryService {
    PartyEntryResponse create(PartyEntryRequest request);
    PartyEntryResponse update(Long id, PartyEntryRequest request);
    PartyEntryResponse findById(Long id);
    Page<PartyEntryResponse> findAll(String search, Pageable pageable);
    void delete(Long id);
    
    List<PartyLookupResponse> findPartiesByClientId(Long clientId);
}
