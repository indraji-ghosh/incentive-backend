package org.example.incentivebackend.module.transaction.bill.service;


import org.example.incentivebackend.module.transaction.bill.dto.request.BillRequest;
import org.example.incentivebackend.module.transaction.bill.dto.response.BillResponse;

public interface BillService {

    BillResponse create(BillRequest request);

    BillResponse findById(Long id);

    BillResponse update(
            Long id,
            BillRequest request
    );

    void delete(Long id);

    org.springframework.data.domain.Page<BillResponse> findAll(String billNumber, Long clientId, Long siteId, org.springframework.data.domain.Pageable pageable);
}