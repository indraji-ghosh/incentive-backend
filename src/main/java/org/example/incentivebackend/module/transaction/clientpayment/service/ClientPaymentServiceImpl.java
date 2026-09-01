package org.example.incentivebackend.module.transaction.clientpayment.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.BusinessValidationException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentHistoryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.BillPaymentSummaryResponse;
import org.example.incentivebackend.module.transaction.clientpayment.dto.response.ClientPaymentResponse;
import org.example.incentivebackend.module.transaction.clientpayment.entity.ClientPaymentEntity;
import org.example.incentivebackend.module.transaction.clientpayment.mapper.ClientPaymentMapper;
import org.example.incentivebackend.module.transaction.clientpayment.repository.ClientPaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientPaymentServiceImpl implements ClientPaymentService {

    private final ClientPaymentRepository clientPaymentRepository;
    private final BillRepository billRepository;
    private final ClientPaymentMapper clientPaymentMapper;

    @Override
    @Transactional
    public ClientPaymentResponse createPayment(ClientPaymentRequest request) {
        BillEntity bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + request.getBillId()));

        BigDecimal totalPaid = clientPaymentRepository.getTotalPaidAmountByBillId(bill.getBillId(), StatusEnum.A);
        BigDecimal outstanding = bill.getBillAmount().subtract(totalPaid);

        if (request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Payment amount must be greater than zero");
        }

        if (request.getPaymentAmount().compareTo(outstanding) > 0) {
            throw new BusinessValidationException("Payment amount exceeds outstanding bill amount. Outstanding: " + outstanding);
        }

        ClientPaymentEntity payment = clientPaymentMapper.toEntity(request);
        payment.setBill(bill);
        payment.setClient(bill.getClient());
        payment.setPaymentStatus(StatusEnum.A);
        payment.setPaymentNo(generatePaymentNo());

        payment = clientPaymentRepository.save(payment);
        return clientPaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public ClientPaymentResponse updatePayment(Long id, ClientPaymentRequest request) {
        ClientPaymentEntity payment = clientPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));

        BillEntity bill = payment.getBill();
        
        BigDecimal totalPaidExcludingCurrent = clientPaymentRepository.getTotalPaidAmountByBillIdExcluding(
                bill.getBillId(), payment.getClientPaymentId(), StatusEnum.A);
        
        BigDecimal outstanding = bill.getBillAmount().subtract(totalPaidExcludingCurrent);

        if (request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Payment amount must be greater than zero");
        }

        if (request.getPaymentAmount().compareTo(outstanding) > 0) {
            throw new BusinessValidationException("Payment amount exceeds outstanding bill amount. Outstanding: " + outstanding);
        }

        clientPaymentMapper.updateEntityFromRequest(request, payment);
        
        payment = clientPaymentRepository.save(payment);
        return clientPaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientPaymentResponse getPaymentById(Long id) {
        ClientPaymentEntity payment = clientPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return clientPaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientPaymentResponse> getAllPayments(Pageable pageable) {
        return clientPaymentRepository.findAll(pageable).map(clientPaymentMapper::toResponse);
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        ClientPaymentEntity payment = clientPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        payment.setPaymentStatus(StatusEnum.I);
        clientPaymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public BillPaymentHistoryResponse getBillPaymentHistory(Long billId) {
        BillEntity bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        List<ClientPaymentEntity> payments = clientPaymentRepository.findActivePaymentsByBillId(billId, StatusEnum.A);
        List<ClientPaymentResponse> paymentResponses = payments.stream()
                .map(clientPaymentMapper::toResponse)
                .collect(Collectors.toList());

        BigDecimal totalPaid = clientPaymentRepository.getTotalPaidAmountByBillId(billId, StatusEnum.A);
        BigDecimal outstanding = bill.getBillAmount().subtract(totalPaid);
        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO;
        }

        String status = determinePaymentStatus(totalPaid, bill.getBillAmount());

        return BillPaymentHistoryResponse.builder()
                .billId(bill.getBillId())
                .billNo(bill.getBillNumber())
                .clientId(bill.getClient().getClientId())
                .clientName(bill.getClient().getClientName())
                .billAmount(bill.getBillAmount())
                .totalPaid(totalPaid)
                .outstandingAmount(outstanding)
                .paymentStatus(status)
                .payments(paymentResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BillPaymentSummaryResponse getBillPaymentSummary(Long billId) {
        BillEntity bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        BigDecimal totalPaid = clientPaymentRepository.getTotalPaidAmountByBillId(billId, StatusEnum.A);
        BigDecimal outstanding = bill.getBillAmount().subtract(totalPaid);
        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO;
        }

        String status = determinePaymentStatus(totalPaid, bill.getBillAmount());

        return BillPaymentSummaryResponse.builder()
                .billId(bill.getBillId())
                .billNo(bill.getBillNumber())
                .billAmount(bill.getBillAmount())
                .totalPaid(totalPaid)
                .outstandingAmount(outstanding)
                .paymentStatus(status)
                .build();
    }

    private String determinePaymentStatus(BigDecimal totalPaid, BigDecimal billAmount) {
        if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            return "UNPAID";
        } else if (totalPaid.compareTo(billAmount) < 0) {
            return "PARTIALLY_PAID";
        } else {
            return "PAID";
        }
    }

    private String generatePaymentNo() {
        // Simple generation logic; can be customized based on existing sequence table if available
        long count = clientPaymentRepository.count();
        return String.format("CP-%06d", count + 1);
    }
}
