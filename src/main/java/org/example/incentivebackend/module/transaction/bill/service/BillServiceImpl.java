package org.example.incentivebackend.module.transaction.bill.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.transaction.bill.dto.BillAnnexureRequest;
import org.example.incentivebackend.module.transaction.bill.dto.request.BillRequest;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillAnnexureResponse;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillResponse;
import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.example.incentivebackend.module.transaction.partyentry.repository.PartyEntryRepository;
import org.example.incentivebackend.module.transaction.bill.dto.*;
import org.example.incentivebackend.module.transaction.bill.entity.BillAnnexureEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    private final ClientRepository clientRepository;

    private final PartyEntryRepository partyRepository;

    @Override
    public BillResponse create(
            BillRequest request
    ) {

        if (billRepository.existsByBillNumberIgnoreCase(
                request.getBillNumber()
        )) {

            throw new DuplicateResourceException(
                    "Bill number already exists"
            );
        }

        ClientEntity client =
                clientRepository.findById(
                        request.getClientId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client not found: "
                                        + request.getClientId()
                        )
                );

        PartyEntryEntity party =
                partyRepository.findById(
                        request.getPartyId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Party not found: "
                                        + request.getPartyId()
                        )
                );

        BillEntity bill =
                new BillEntity();

        bill.setBillNumber(
                request.getBillNumber()
        );

        bill.setWorkingMonth(
                request.getWorkingMonth()
        );

        bill.setClient(client);

        bill.setParty(party);

        bill.setBillAmount(
                request.getBillAmount()
        );

        bill.setRemarks(
                request.getRemarks()
        );

        List<BillAnnexureEntity> annexures =
                new ArrayList<>();

        for (
                BillAnnexureRequest row :
                request.getAnnexures()
        ) {

            BillAnnexureEntity annexure =
                    new BillAnnexureEntity();

            annexure.setBill(bill);

            annexure.setRrNo(row.getRrNo());

            annexure.setRrDate(row.getRrDate());

            annexure.setChallan(row.getChallan());

            annexure.setLoadDate(row.getLoadDate());

            annexure.setSiding(row.getSiding());

            annexure.setDestination(
                    row.getDestination()
            );

            annexure.setWagons(
                    row.getWagons()
            );

            annexures.add(annexure);
        }

        bill.setAnnexures(annexures);

        BillEntity saved =
                billRepository.save(bill);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse findById(Long id) {

        BillEntity bill =
                billRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bill not found: " + id
                                )
                        );

        return toResponse(bill);
    }

    @Override
    public BillResponse update(
            Long id,
            BillRequest request
    ) {

        BillEntity bill =
                billRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bill not found: " + id
                                )
                        );

        if (!bill.getBillNumber()
                .equalsIgnoreCase(
                        request.getBillNumber()
                )
                && billRepository
                .existsByBillNumberIgnoreCase(
                        request.getBillNumber()
                )) {

            throw new DuplicateResourceException(
                    "Bill number already exists"
            );
        }

        ClientEntity client =
                clientRepository.findById(
                        request.getClientId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Client not found"
                        )
                );

        PartyEntryEntity party =
                partyRepository.findById(
                        request.getPartyId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Party not found"
                        )
                );

        bill.setBillNumber(
                request.getBillNumber()
        );

        bill.setWorkingMonth(
                request.getWorkingMonth()
        );

        bill.setClient(client);

        bill.setParty(party);

        bill.setBillAmount(
                request.getBillAmount()
        );

        bill.setRemarks(
                request.getRemarks()
        );

        bill.getAnnexures().clear();

        for (
                BillAnnexureRequest row :
                request.getAnnexures()
        ) {

            BillAnnexureEntity annexure =
                    new BillAnnexureEntity();

            annexure.setBill(bill);
            annexure.setRrNo(row.getRrNo());
            annexure.setRrDate(row.getRrDate());
            annexure.setChallan(row.getChallan());
            annexure.setLoadDate(row.getLoadDate());
            annexure.setSiding(row.getSiding());
            annexure.setDestination(row.getDestination());
            annexure.setWagons(row.getWagons());

            bill.getAnnexures().add(annexure);
        }

        return toResponse(bill);
    }

    @Override
    public void delete(Long id) {

        BillEntity bill =
                billRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bill not found: " + id
                                )
                        );

        billRepository.delete(bill);
    }

    private BillResponse toResponse(
            BillEntity bill
    ) {

        List<BillAnnexureResponse> rows =
                bill.getAnnexures()
                        .stream()
                        .map(row ->
                                BillAnnexureResponse.builder()
                                        .billAnnexureId(
                                                row.getBillAnnexureId()
                                        )
                                        .rrNo(row.getRrNo())
                                        .rrDate(row.getRrDate())
                                        .challan(row.getChallan())
                                        .loadDate(row.getLoadDate())
                                        .siding(row.getSiding())
                                        .destination(
                                                row.getDestination()
                                        )
                                        .wagons(row.getWagons())
                                        .build()
                        )
                        .toList();

        int totalWagons =
                rows.stream()
                        .mapToInt(
                                row -> row.getWagons() == null
                                        ? 0
                                        : row.getWagons()
                        )
                        .sum();

        return BillResponse.builder()
                .billId(bill.getBillId())
                .billNumber(bill.getBillNumber())
                .workingMonth(bill.getWorkingMonth())
                .clientId(
                        bill.getClient().getClientId()
                )
                .clientName(
                        bill.getClient().getClientName()
                )
                .partyId(
                        bill.getParty().getId()
                )
                .partyName(
                        bill.getParty().getPartyName()
                )
                .billAmount(bill.getBillAmount())
                .remarks(bill.getRemarks())
                .totalRr(rows.size())
                .totalWagons(totalWagons)
                .annexures(rows)
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public org.springframework.data.domain.Page<BillResponse> findAll(String search, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<org.example.incentivebackend.module.transaction.bill.entity.BillEntity> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("billNumber")), likePattern),
                    cb.like(cb.lower(root.get("remarks")), likePattern)
            ));
        }
        return billRepository.findAll(spec, pageable).map(this::toResponse);
    }
}