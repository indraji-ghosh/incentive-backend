package org.example.incentivebackend.module.transaction.rakeEntry.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeAnnexureRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.request.RakeEntryRequest;
import org.example.incentivebackend.module.transaction.rakeEntry.dto.response.RakeEntryResponse;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.mapper.RakeEntryMapper;
import org.example.incentivebackend.module.transaction.rakeEntry.repository.RakeEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RakeEntryServiceImpl implements RakeEntryService {

    private final RakeEntryRepository rakeEntryRepository;
    private final ClientRepository clientRepository;
    private final PartyEntryRepository partyRepository;
    private final RakeEntryMapper rakeEntryMapper;

    @Override
    public RakeEntryResponse create(RakeEntryRequest request) {
        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        PartyEntryEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found"));

        RakeEntryEntity rakeEntry = rakeEntryMapper.toEntity(request);
        rakeEntry.setClient(client);
        rakeEntry.setParty(party);
        rakeEntry.setRakeStatus(StatusEnum.A); // Assuming default active status

        for (RakeAnnexureRequest annexureReq : request.getAnnexures()) {
            RakeAnnexureEntity annexure = rakeEntryMapper.toAnnexureEntity(annexureReq);
            annexure.setRakeEntry(rakeEntry);
            rakeEntry.getAnnexures().add(annexure);
        }

        RakeEntryEntity saved = rakeEntryRepository.save(rakeEntry);
        return rakeEntryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RakeEntryResponse> findAll() {
        return rakeEntryMapper.toResponseList(rakeEntryRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public RakeEntryResponse findById(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        return rakeEntryMapper.toResponse(rakeEntry);
    }

    @Override
    public RakeEntryResponse update(Long id, RakeEntryRequest request) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));

        PartyEntryEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found"));

        rakeEntryMapper.updateEntityFromRequest(request, rakeEntry);
        rakeEntry.setClient(client);
        rakeEntry.setParty(party);

        rakeEntry.getAnnexures().clear();
        for (RakeAnnexureRequest annexureReq : request.getAnnexures()) {
            RakeAnnexureEntity annexure = rakeEntryMapper.toAnnexureEntity(annexureReq);
            annexure.setRakeEntry(rakeEntry);
            rakeEntry.getAnnexures().add(annexure);
        }

        return rakeEntryMapper.toResponse(rakeEntry);
    }

    @Override
    public void softDelete(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        rakeEntry.setRakeStatus(StatusEnum.I);
    }

    @Override
    public void hardDelete(Long id) {
        RakeEntryEntity rakeEntry = rakeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rake entry not found"));
        rakeEntryRepository.delete(rakeEntry);
    }
}
