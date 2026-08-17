package org.example.incentivebackend.module.master.paymenttype.service;

import org.example.incentivebackend.common.DropdownDTO;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeRequest;
import org.example.incentivebackend.module.master.paymenttype.dto.PaymentTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentTypeService {
    PaymentTypeResponse create(PaymentTypeRequest request);
    PaymentTypeResponse update(Long id, PaymentTypeRequest request);
    PaymentTypeResponse findById(Long id);
    Page<PaymentTypeResponse> findAll(String search, Pageable pageable);
    void delete(Long id);
    List<DropdownDTO> getDropdown();
}
