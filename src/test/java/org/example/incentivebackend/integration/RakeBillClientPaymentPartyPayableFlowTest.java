package org.example.incentivebackend.integration;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.BusinessValidationException;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyAssignmentRepository;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyServiceConfigurationRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.bill.enums.BillPaymentStatus;
import org.example.incentivebackend.module.transaction.bill.repository.BillRepository;
import org.example.incentivebackend.module.transaction.clientpayment.dto.request.ClientPaymentRequest;
import org.example.incentivebackend.module.transaction.clientpayment.entity.ClientPaymentEntity;
import org.example.incentivebackend.module.transaction.clientpayment.mapper.ClientPaymentMapper;
import org.example.incentivebackend.module.transaction.clientpayment.repository.ClientPaymentRepository;
import org.example.incentivebackend.module.transaction.clientpayment.service.ClientPaymentServiceImpl;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.module.transaction.partypayable.service.PartyPayableServiceImpl;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RakeBillClientPaymentPartyPayableFlowTest {

    @Mock
    private PartyPayableRepository partyPayableRepository;

    @Mock
    private PartyAssignmentRepository partyAssignmentRepository;

    @Mock
    private PartyServiceConfigurationRepository configurationRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private ClientPaymentRepository clientPaymentRepository;

    @Mock
    private ClientPaymentMapper clientPaymentMapper;

    @InjectMocks
    private PartyPayableServiceImpl partyPayableService;

    @InjectMocks
    private ClientPaymentServiceImpl clientPaymentService;

    private ClientEntity ultraTech;
    private SiteEntity durgapurSiding;
    private PartyEntity maaTara;
    private PartyEntity bengalCovering;
    private PartyAssignmentEntity maaTaraAssignment;
    private PartyAssignmentEntity bengalAssignment;

    private ServiceTypeEntity rakeCoveringService;
    private ServiceTypeEntity rakeUncoveringService;
    private ServiceTypeEntity doorPastingService;

    private PaymentTypeEntity rakeBased;
    private PaymentTypeEntity wagonBased;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(clientPaymentService, "partyPayableService", partyPayableService);
        ultraTech = new ClientEntity();
        ultraTech.setClientId(1L);
        ultraTech.setClientName("UltraTech Cement");

        durgapurSiding = new SiteEntity();
        durgapurSiding.setSiteId(10L);
        durgapurSiding.setSiteName("Durgapur Siding");

        maaTara = new PartyEntity();
        maaTara.setId(101L);
        maaTara.setPartyName("Maa Tara Services");

        bengalCovering = new PartyEntity();
        bengalCovering.setId(102L);
        bengalCovering.setPartyName("Bengal Covering Works");

        maaTaraAssignment = new PartyAssignmentEntity();
        maaTaraAssignment.setId(1001L);
        maaTaraAssignment.setClient(ultraTech);
        maaTaraAssignment.setSite(durgapurSiding);
        maaTaraAssignment.setParty(maaTara);
        maaTaraAssignment.setStatus(StatusEnum.A);

        bengalAssignment = new PartyAssignmentEntity();
        bengalAssignment.setId(1002L);
        bengalAssignment.setClient(ultraTech);
        bengalAssignment.setSite(durgapurSiding);
        bengalAssignment.setParty(bengalCovering);
        bengalAssignment.setStatus(StatusEnum.A);

        rakeCoveringService = new ServiceTypeEntity();
        rakeCoveringService.setId(11L);
        rakeCoveringService.setName("Rake Covering");

        rakeUncoveringService = new ServiceTypeEntity();
        rakeUncoveringService.setId(12L);
        rakeUncoveringService.setName("Rake Uncovering");

        doorPastingService = new ServiceTypeEntity();
        doorPastingService.setId(13L);
        doorPastingService.setName("Door Pasting");

        rakeBased = new PaymentTypeEntity();
        rakeBased.setId(201L);
        rakeBased.setCode("RAKE_BASED");

        wagonBased = new PaymentTypeEntity();
        wagonBased.setId(202L);
        wagonBased.setCode("WAGON_BASED");
    }

    private PartyServiceConfigurationEntity createConfig(
            Long id,
            PartyAssignmentEntity assignment,
            ServiceTypeEntity service,
            PaymentTypeEntity paymentType,
            BigDecimal rate
    ) {
        PartyServiceConfigurationEntity cfg = new PartyServiceConfigurationEntity();
        cfg.setId(id);
        cfg.setPartyAssignment(assignment);
        cfg.setService(service);
        cfg.setPaymentType(paymentType);
        cfg.setRate(rate);
        cfg.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        cfg.setEffectiveTo(null);
        cfg.setStatus(StatusEnum.A);
        return cfg;
    }

    @Test
    @DisplayName("Section 12 Demo Test: Rake RK-2026-0001 with single service creates 2 party payables")
    void testRakeDemoRK20260001_GeneratesPayablesForSingleService() {
        // Maa Tara: Covering ₹2,200/Rake, Uncovering ₹1,500/Rake, Door Pasting ₹50/Wagon
        PartyServiceConfigurationEntity cfg1 = createConfig(1L, maaTaraAssignment, rakeCoveringService, rakeBased, new BigDecimal("2200.00"));
        PartyServiceConfigurationEntity cfg2 = createConfig(2L, maaTaraAssignment, rakeUncoveringService, rakeBased, new BigDecimal("1500.00"));
        PartyServiceConfigurationEntity cfg3 = createConfig(3L, maaTaraAssignment, doorPastingService, wagonBased, new BigDecimal("50.00"));

        // Bengal Covering: Covering ₹1,800/Rake
        PartyServiceConfigurationEntity cfg4 = createConfig(4L, bengalAssignment, rakeCoveringService, rakeBased, new BigDecimal("1800.00"));

        RakeEntryEntity rake = new RakeEntryEntity();
        rake.setRakeEntryId(9001L);
        rake.setRakeNumber("RK-2026-0001");
        rake.setClient(ultraTech);
        rake.setSite(durgapurSiding);
        rake.setWorkingMonth(LocalDate.of(2026, 9, 5));
        rake.setService(rakeCoveringService);

        RakeAnnexureEntity ann = new RakeAnnexureEntity();
        ann.setRrDate(LocalDate.of(2026, 9, 5));
        ann.setRrNo("RK-2026-0001");
        ann.setWagons(60);
        ann.setWeight(new BigDecimal("1500.00"));
        rake.setAnnexures(List.of(ann));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(1L, 10L, StatusEnum.A))
                .thenReturn(List.of(maaTaraAssignment, bengalAssignment));

        when(configurationRepository.findEffectiveConfigurations(1001L, LocalDate.of(2026, 9, 5), StatusEnum.A))
                .thenReturn(List.of(cfg1, cfg2, cfg3));

        when(configurationRepository.findEffectiveConfigurations(1002L, LocalDate.of(2026, 9, 5), StatusEnum.A))
                .thenReturn(List.of(cfg4));

        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(false);

        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PartyPayableEntity> payables = partyPayableService.generatePayablesForRake(rake);

        assertNotNull(payables);
        assertEquals(2, payables.size(), "Must generate exactly 2 Party Payable records for the covering service");

        BigDecimal total = payables.stream()
                .map(PartyPayableEntity::getPayableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("4000.00").compareTo(total), "Expected total payable = ₹4,000 (2200 + 1800)");

        // Verify Maa Tara Covering: 1 x 2200 = 2200
        PartyPayableEntity p1 = payables.stream()
                .filter(p -> p.getParty().getId().equals(101L) && p.getService().getId().equals(11L))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("2200.00").compareTo(p1.getPayableAmount()));

        // Verify Bengal Covering: 1 x 1800 = 1800
        PartyPayableEntity p4 = payables.stream()
                .filter(p -> p.getParty().getId().equals(102L) && p.getService().getId().equals(11L))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("1800.00").compareTo(p4.getPayableAmount()));

        // Verify Idempotency on repeated approval:
        reset(partyPayableRepository);
        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(1L, 10L, StatusEnum.A))
                .thenReturn(List.of(maaTaraAssignment, bengalAssignment));
        when(configurationRepository.findEffectiveConfigurations(1001L, LocalDate.of(2026, 9, 5), StatusEnum.A))
                .thenReturn(List.of(cfg1, cfg2, cfg3));
        when(configurationRepository.findEffectiveConfigurations(1002L, LocalDate.of(2026, 9, 5), StatusEnum.A))
                .thenReturn(List.of(cfg4));
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(true);

        List<PartyPayableEntity> secondRun = partyPayableService.generatePayablesForRake(rake);
        assertEquals(0, secondRun.size(), "Idempotent: should not generate duplicate payables on re-approval");
        verify(partyPayableRepository, never()).save(any());
    }

    @Test
    @DisplayName("Section 13 Demo Test: Client Bill BILL-2026-0001 (₹1,00,000) with 4 partial payments reaches PAID")
    void testClientBillDemo_MultiPayment_PartiallyPaidToPaid() {
        BillEntity bill = new BillEntity();
        bill.setBillId(501L);
        bill.setBillNumber("BILL-2026-0001");
        bill.setClient(ultraTech);
        bill.setBillAmount(new BigDecimal("100000.00"));
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setOutstandingAmount(new BigDecimal("100000.00"));
        bill.setPaymentStatus(BillPaymentStatus.UNPAID);

        when(billRepository.findById(501L)).thenReturn(Optional.of(bill));
        when(clientPaymentMapper.toEntity(any())).thenAnswer(inv -> {
            ClientPaymentRequest req = inv.getArgument(0);
            ClientPaymentEntity entity = new ClientPaymentEntity();
            entity.setPaymentAmount(req.getPaymentAmount());
            return entity;
        });
        when(clientPaymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Payment 1: ₹30,000
        when(clientPaymentRepository.getTotalPaidAmountByBillId(501L, StatusEnum.A))
                .thenReturn(BigDecimal.ZERO) // prior total before p1
                .thenReturn(new BigDecimal("30000.00")); // after p1

        ClientPaymentRequest p1 = new ClientPaymentRequest();
        p1.setBillId(501L);
        p1.setPaymentAmount(new BigDecimal("30000.00"));
        clientPaymentService.createPayment(p1);

        assertEquals(0, new BigDecimal("30000.00").compareTo(bill.getPaidAmount()));
        assertEquals(0, new BigDecimal("70000.00").compareTo(bill.getOutstandingAmount()));
        assertEquals(BillPaymentStatus.PARTIALLY_PAID, bill.getPaymentStatus());

        // Payment 2: ₹20,000
        when(clientPaymentRepository.getTotalPaidAmountByBillId(501L, StatusEnum.A))
                .thenReturn(new BigDecimal("30000.00"))
                .thenReturn(new BigDecimal("50000.00"));

        ClientPaymentRequest p2 = new ClientPaymentRequest();
        p2.setBillId(501L);
        p2.setPaymentAmount(new BigDecimal("20000.00"));
        clientPaymentService.createPayment(p2);

        assertEquals(0, new BigDecimal("50000.00").compareTo(bill.getPaidAmount()));
        assertEquals(0, new BigDecimal("50000.00").compareTo(bill.getOutstandingAmount()));
        assertEquals(BillPaymentStatus.PARTIALLY_PAID, bill.getPaymentStatus());

        // Payment 3: ₹10,000
        when(clientPaymentRepository.getTotalPaidAmountByBillId(501L, StatusEnum.A))
                .thenReturn(new BigDecimal("50000.00"))
                .thenReturn(new BigDecimal("60000.00"));

        ClientPaymentRequest p3 = new ClientPaymentRequest();
        p3.setBillId(501L);
        p3.setPaymentAmount(new BigDecimal("10000.00"));
        clientPaymentService.createPayment(p3);

        assertEquals(0, new BigDecimal("60000.00").compareTo(bill.getPaidAmount()));
        assertEquals(0, new BigDecimal("40000.00").compareTo(bill.getOutstandingAmount()));
        assertEquals(BillPaymentStatus.PARTIALLY_PAID, bill.getPaymentStatus());

        // Overpayment attempt: ₹50,000 > ₹40,000 outstanding -> must throw BusinessValidationException
        when(clientPaymentRepository.getTotalPaidAmountByBillId(501L, StatusEnum.A))
                .thenReturn(new BigDecimal("60000.00"));

        ClientPaymentRequest overpayment = new ClientPaymentRequest();
        overpayment.setBillId(501L);
        overpayment.setPaymentAmount(new BigDecimal("50000.00"));
        assertThrows(BusinessValidationException.class, () -> clientPaymentService.createPayment(overpayment));

        // Payment 4: ₹40,000 -> completes bill, status becomes PAID
        when(clientPaymentRepository.getTotalPaidAmountByBillId(501L, StatusEnum.A))
                .thenReturn(new BigDecimal("60000.00"))
                .thenReturn(new BigDecimal("100000.00"));

        ClientPaymentRequest p4 = new ClientPaymentRequest();
        p4.setBillId(501L);
        p4.setPaymentAmount(new BigDecimal("40000.00"));
        clientPaymentService.createPayment(p4);

        assertEquals(0, new BigDecimal("100000.00").compareTo(bill.getPaidAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(bill.getOutstandingAmount()));
        assertEquals(BillPaymentStatus.PAID, bill.getPaymentStatus(), "Bill status should transition to PAID");
    }
}
