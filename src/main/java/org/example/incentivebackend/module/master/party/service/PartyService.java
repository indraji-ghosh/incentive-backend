package org.example.incentivebackend.module.master.party.service;

import org.example.incentivebackend.module.master.party.dto.request.PartyRequest;
import org.example.incentivebackend.module.master.party.dto.response.PartyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartyService {

    PartyResponse create(PartyRequest request);

    PartyResponse update(Long id, PartyRequest request);

    PartyResponse findById(Long id);

    Page<PartyResponse> findAll(String search, Pageable pageable);

    List<PartyResponse> findAllActive();

    void delete(Long id);
}
