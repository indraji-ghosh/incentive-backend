package org.example.incentivebackend.module.transaction.partypayable.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyAssignmentRepository;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyServiceConfigurationRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillAnnexureEntity;
import org.example.incentivebackend.module.transaction.bill.entity.BillEntity;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.example.incentivebackend.module.transaction.partypayable.mapper.PartyPayableMapper;
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyPayableServiceImplTest {

    @Mock
    private PartyPayableRepository partyPayableRepository;

    @Mock
    private PartyAssignmentRepository partyAssignmentRepository;

    @Mock
    private PartyServiceConfigurationRepository configurationRepository;

    @Mock
    private PartyRepository partyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ServiceTypeRepository serviceRepository;

    @Mock
    private org.example.incentivebackend.module.transaction.bill.repository.BillRepository billRepository;

    @Mock
    private PartyPayableMapper partyPayableMapper;

    @InjectMocks
    private PartyPayableServiceImpl partyPayableService;

    private ClientEntity client;
    private SiteEntity site;
    private PartyEntity partyA;
    private PartyEntity partyB;
    private PartyEntity partyC;
    private PartyAssignmentEntity assignmentA;
    private PartyAssignmentEntity assignmentB;
    private PartyAssignmentEntity assignmentC;
    private ServiceTypeEntity coveringService;
    private ServiceTypeEntity uncoveringService;
    private ServiceTypeEntity doorPastingService;
    private ServiceTypeEntity supervisorService;
    private PaymentTypeEntity rakeBased;
    private PaymentTypeEntity wagonBased;
    private PaymentTypeEntity mtBased;
    private PaymentTypeEntity monthlyFixed;
    private UnitEntity rakeUnit;
    private UnitEntity wagonUnit;
    private UnitEntity mtUnit;
    private UnitEntity monthUnit;

    @BeforeEach
    void setUp() {
        client = new ClientEntity();
        client.setClientId(10L);
        client.setClientName("UltraTech");
        client.setClientStatus(StatusEnum.A);

        site = new SiteEntity();
        site.setSiteId(100L);
        site.setSiteName("Site A");
        site.setSiteStatus(StatusEnum.A);

        partyA = new PartyEntity();
        partyA.setId(1L);
        partyA.setPartyName("Party A");
        partyA.setPartyStatus(StatusEnum.A);

        partyB = new PartyEntity();
        partyB.setId(2L);
        partyB.setPartyName("Party B");
        partyB.setPartyStatus(StatusEnum.A);

        partyC = new PartyEntity();
        partyC.setId(3L);
        partyC.setPartyName("Party C");
        partyC.setPartyStatus(StatusEnum.A);

        assignmentA = new PartyAssignmentEntity();
        assignmentA.setId(101L);
        assignmentA.setParty(partyA);
        assignmentA.setClient(client);
        assignmentA.setSite(site);
        assignmentA.setStatus(StatusEnum.A);

        assignmentB = new PartyAssignmentEntity();
        assignmentB.setId(102L);
        assignmentB.setParty(partyB);
        assignmentB.setClient(client);
        assignmentB.setSite(site);
        assignmentB.setStatus(StatusEnum.A);

        assignmentC = new PartyAssignmentEntity();
        assignmentC.setId(103L);
        assignmentC.setParty(partyC);
        assignmentC.setClient(client);
        assignmentC.setSite(site);
        assignmentC.setStatus(StatusEnum.A);

        coveringService = new ServiceTypeEntity();
        coveringService.setId(1L);
        coveringService.setName("Covering");

        uncoveringService = new ServiceTypeEntity();
        uncoveringService.setId(2L);
        uncoveringService.setName("Uncovering");

        doorPastingService = new ServiceTypeEntity();
        doorPastingService.setId(3L);
        doorPastingService.setName("Door Pasting");

        supervisorService = new ServiceTypeEntity();
        supervisorService.setId(4L);
        supervisorService.setName("Supervisor");

        rakeBased = new PaymentTypeEntity();
        rakeBased.setId(501L);
        rakeBased.setCode("RAKE_BASED");
        rakeBased.setName("Rake Based");

        wagonBased = new PaymentTypeEntity();
        wagonBased.setId(502L);
        wagonBased.setCode("WAGON_BASED");
        wagonBased.setName("Wagon Based");

        mtBased = new PaymentTypeEntity();
        mtBased.setId(503L);
        mtBased.setCode("MT_BASED");
        mtBased.setName("MT Based");

        monthlyFixed = new PaymentTypeEntity();
        monthlyFixed.setId(504L);
        monthlyFixed.setCode("MONTHLY_FIXED");
        monthlyFixed.setName("Monthly Fixed");

        rakeUnit = new UnitEntity();
        rakeUnit.setId(601L);
        rakeUnit.setCode("RAKE");

        wagonUnit = new UnitEntity();
        wagonUnit.setId(602L);
        wagonUnit.setCode("WAGON");

        mtUnit = new UnitEntity();
        mtUnit.setId(603L);
        mtUnit.setCode("MT");

        monthUnit = new UnitEntity();
        monthUnit.setId(604L);
        monthUnit.setCode("MONTH");
    }

    private PartyServiceConfigurationEntity buildConfig(
            Long id,
            PartyAssignmentEntity assignment,
            ServiceTypeEntity service,
            PaymentTypeEntity paymentType,
            BigDecimal rate,
            LocalDate from,
            LocalDate to
    ) {
        PartyServiceConfigurationEntity cfg = new PartyServiceConfigurationEntity();
        cfg.setId(id);
        cfg.setPartyAssignment(assignment);
        cfg.setService(service);
        cfg.setPaymentType(paymentType);
        cfg.setRate(rate);
        cfg.setEffectiveFrom(from);
        cfg.setEffectiveTo(to);
        cfg.setStatus(StatusEnum.A);
        return cfg;
    }

    @Test
    @DisplayName("Requirements 6, 7, 8: Effective date configuration selection - Old rate for old rake, New rate for new rake")
    void testEffectiveDateRateSelection() {
        // Configuration 1: 2026-07-01 to 2026-07-31 -> rate 2000
        PartyServiceConfigurationEntity oldCfg = buildConfig(1L, assignmentA, coveringService, rakeBased,
                new BigDecimal("2000.00"), LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        // Configuration 2: 2026-08-01 to null -> rate 2200
        PartyServiceConfigurationEntity newCfg = buildConfig(2L, assignmentA, coveringService, rakeBased,
                new BigDecimal("2200.00"), LocalDate.of(2026, 8, 1), null);

        // Old Rake on July 20, 2026
        RakeEntryEntity oldRake = new RakeEntryEntity();
        oldRake.setRakeEntryId(1001L);
        oldRake.setClient(client);
        oldRake.setSite(site);
        oldRake.setWorkingMonth(LocalDate.of(2026, 7, 20));
        oldRake.setRakeStatus(StatusEnum.A);

        RakeAnnexureEntity ann1 = new RakeAnnexureEntity();
        ann1.setRakeEntry(oldRake);
        ann1.setRrDate(LocalDate.of(2026, 7, 20));
        ann1.setRrNo("RR-1001");
        ann1.setWagons(58);
        oldRake.setAnnexures(List.of(ann1));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(10L, 100L, StatusEnum.A))
                .thenReturn(List.of(assignmentA));
        when(configurationRepository.findEffectiveConfigurations(101L, LocalDate.of(2026, 7, 20), StatusEnum.A))
                .thenReturn(List.of(oldCfg));
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(false);
        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Generate for old rake
        partyPayableService.generatePayablesForRake(oldRake);

        ArgumentCaptor<PartyPayableEntity> captor = ArgumentCaptor.forClass(PartyPayableEntity.class);
        verify(partyPayableRepository).save(captor.capture());
        PartyPayableEntity oldPayable = captor.getValue();
        assertEquals(0, new BigDecimal("2000.00").compareTo(oldPayable.getRate()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(oldPayable.getPayableAmount()));
        assertEquals(LocalDate.of(2026, 7, 20), oldPayable.getTransactionDate());

        // Now Rake on August 20, 2026
        reset(partyPayableRepository);
        RakeEntryEntity newRake = new RakeEntryEntity();
        newRake.setRakeEntryId(1002L);
        newRake.setClient(client);
        newRake.setSite(site);
        newRake.setWorkingMonth(LocalDate.of(2026, 8, 20));
        newRake.setRakeStatus(StatusEnum.A);

        RakeAnnexureEntity ann2 = new RakeAnnexureEntity();
        ann2.setRakeEntry(newRake);
        ann2.setRrDate(LocalDate.of(2026, 8, 20));
        ann2.setRrNo("RR-1002");
        ann2.setWagons(58);
        newRake.setAnnexures(List.of(ann2));

        when(configurationRepository.findEffectiveConfigurations(101L, LocalDate.of(2026, 8, 20), StatusEnum.A))
                .thenReturn(List.of(newCfg));
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(false);
        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        partyPayableService.generatePayablesForRake(newRake);

        ArgumentCaptor<PartyPayableEntity> captorNew = ArgumentCaptor.forClass(PartyPayableEntity.class);
        verify(partyPayableRepository).save(captorNew.capture());
        PartyPayableEntity newPayable = captorNew.getValue();
        assertEquals(0, new BigDecimal("2200.00").compareTo(newPayable.getRate()));
        assertEquals(0, new BigDecimal("2200.00").compareTo(newPayable.getPayableAmount()));
        assertEquals(LocalDate.of(2026, 8, 20), newPayable.getTransactionDate());
    }

    @Test
    @DisplayName("Requirements 9, 10, 11: One Rake generating payables for multiple parties (RAKE_BASED, WAGON_BASED)")
    void testRakeGeneratingPayablesForMultipleParties() {
        // Party A: Covering -> 2000 / Rake
        PartyServiceConfigurationEntity cfgA = buildConfig(1L, assignmentA, coveringService, rakeBased,
                new BigDecimal("2000.00"), LocalDate.of(2026, 1, 1), null);

        // Party B: Uncovering -> 1000 / Rake
        PartyServiceConfigurationEntity cfgB = buildConfig(2L, assignmentB, uncoveringService, rakeBased,
                new BigDecimal("1000.00"), LocalDate.of(2026, 1, 1), null);

        // Party C: Door Pasting -> 500 / Wagon (60 wagons -> 30,000)
        PartyServiceConfigurationEntity cfgC = buildConfig(3L, assignmentC, doorPastingService, wagonBased,
                new BigDecimal("500.00"), LocalDate.of(2026, 1, 1), null);

        RakeEntryEntity rake = new RakeEntryEntity();
        rake.setRakeEntryId(5001L);
        rake.setClient(client);
        rake.setSite(site);
        rake.setWorkingMonth(LocalDate.of(2026, 9, 1));
        rake.setRakeStatus(StatusEnum.A);

        RakeAnnexureEntity ann = new RakeAnnexureEntity();
        ann.setRrDate(LocalDate.of(2026, 9, 1));
        ann.setRrNo("RR-5001");
        ann.setWagons(60);
        rake.setAnnexures(List.of(ann));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(10L, 100L, StatusEnum.A))
                .thenReturn(List.of(assignmentA, assignmentB, assignmentC));

        when(configurationRepository.findEffectiveConfigurations(101L, LocalDate.of(2026, 9, 1), StatusEnum.A))
                .thenReturn(List.of(cfgA));
        when(configurationRepository.findEffectiveConfigurations(102L, LocalDate.of(2026, 9, 1), StatusEnum.A))
                .thenReturn(List.of(cfgB));
        when(configurationRepository.findEffectiveConfigurations(103L, LocalDate.of(2026, 9, 1), StatusEnum.A))
                .thenReturn(List.of(cfgC));

        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(false);
        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PartyPayableEntity> payables = partyPayableService.generatePayablesForRake(rake);

        assertNotNull(payables);
        assertEquals(3, payables.size());

        // Verify Party A: 2000
        PartyPayableEntity payableA = payables.stream()
                .filter(p -> p.getParty().getId().equals(1L))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("2000.00").compareTo(payableA.getPayableAmount()));
        assertEquals("RAKE_BASED", payableA.getCalculationBasis());
        assertEquals(0, BigDecimal.ONE.compareTo(payableA.getQuantity()));

        // Verify Party B: 1000
        PartyPayableEntity payableB = payables.stream()
                .filter(p -> p.getParty().getId().equals(2L))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(payableB.getPayableAmount()));
        assertEquals("RAKE_BASED", payableB.getCalculationBasis());
        assertEquals(0, BigDecimal.ONE.compareTo(payableB.getQuantity()));

        // Verify Party C: 500 * 60 = 30000
        PartyPayableEntity payableC = payables.stream()
                .filter(p -> p.getParty().getId().equals(3L))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("30000.00").compareTo(payableC.getPayableAmount()));
        assertEquals("WAGON_BASED", payableC.getCalculationBasis());
        assertEquals(0, new BigDecimal("60").compareTo(payableC.getQuantity()));
    }

    @Test
    @DisplayName("Requirement 12: MT-based calculation")
    void testMtBasedCalculation() {
        // Party A: Covering -> 20 / MT
        PartyServiceConfigurationEntity cfgMT = buildConfig(1L, assignmentA, coveringService, mtBased,
                new BigDecimal("20.00"), LocalDate.of(2026, 1, 1), null);

        RakeEntryEntity rake = new RakeEntryEntity();
        rake.setRakeEntryId(5002L);
        rake.setClient(client);
        rake.setSite(site);
        rake.setWorkingMonth(LocalDate.of(2026, 9, 2));
        rake.setRakeStatus(StatusEnum.A);

        RakeAnnexureEntity ann = new RakeAnnexureEntity();
        ann.setRrDate(LocalDate.of(2026, 9, 2));
        ann.setRrNo("RR-5002");
        ann.setWagons(50);
        ann.setWeight(new BigDecimal("3500.50")); // 3,500.50 MT
        rake.setAnnexures(List.of(ann));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(10L, 100L, StatusEnum.A))
                .thenReturn(List.of(assignmentA));
        when(configurationRepository.findEffectiveConfigurations(101L, LocalDate.of(2026, 9, 2), StatusEnum.A))
                .thenReturn(List.of(cfgMT));
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(any(), any(), any(), any()))
                .thenReturn(false);
        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PartyPayableEntity> payables = partyPayableService.generatePayablesForRake(rake);

        assertNotNull(payables);
        assertEquals(1, payables.size());
        PartyPayableEntity p = payables.get(0);
        // 3500.50 * 20.00 = 70010.00
        assertEquals(0, new BigDecimal("70010.00").compareTo(p.getPayableAmount()));
        assertEquals("MT_BASED", p.getCalculationBasis());
        assertEquals(0, new BigDecimal("3500.50").compareTo(p.getQuantity()));
    }

    @Test
    @DisplayName("Requirements 13 & 14: Monthly fixed payable generation & Scheduler idempotency")
    void testMonthlyFixedPayableGenerationAndIdempotency() {
        // Party A: Supervisor -> MONTHLY_FIXED 50,000 / Month
        PartyServiceConfigurationEntity monthlyCfg = buildConfig(10L, assignmentA, supervisorService, monthlyFixed,
                new BigDecimal("50000.00"), LocalDate.of(2026, 1, 1), null);

        LocalDate targetDate = LocalDate.of(2026, 9, 1);
        String periodKey = "2026-09";

        when(configurationRepository.findActiveMonthlyFixedConfigurations(targetDate, StatusEnum.A))
                .thenReturn(List.of(monthlyCfg));

        // First run: payable does not exist
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                "MONTHLY_FIXED", periodKey, 101L, 4L
        )).thenReturn(false);
        when(partyPayableRepository.save(any(PartyPayableEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PartyPayableEntity> generatedFirstTime = partyPayableService.createMonthlyFixedPayables(targetDate);

        assertEquals(1, generatedFirstTime.size());
        PartyPayableEntity p = generatedFirstTime.get(0);
        assertEquals(0, new BigDecimal("50000.00").compareTo(p.getPayableAmount()));
        assertEquals("MONTHLY_FIXED", p.getCalculationBasis());
        assertEquals(LocalDate.of(2026, 9, 1), p.getTransactionDate());
        assertEquals(periodKey, p.getSourceId());

        // Second run: scheduler runs again for same month -> must be idempotent!
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                "MONTHLY_FIXED", periodKey, 101L, 4L
        )).thenReturn(true);

        List<PartyPayableEntity> generatedSecondTime = partyPayableService.createMonthlyFixedPayables(targetDate);

        assertTrue(generatedSecondTime.isEmpty());
        // Verify save was only called once in total across both executions
        verify(partyPayableRepository, times(1)).save(any(PartyPayableEntity.class));
    }

    @Test
    @DisplayName("Requirement 15: Duplicate rake payable prevention")
    void testDuplicateRakePayablePrevention() {
        PartyServiceConfigurationEntity cfg = buildConfig(1L, assignmentA, coveringService, rakeBased,
                new BigDecimal("2000.00"), LocalDate.of(2026, 1, 1), null);

        RakeEntryEntity rake = new RakeEntryEntity();
        rake.setRakeEntryId(999L);
        rake.setClient(client);
        rake.setSite(site);
        rake.setWorkingMonth(LocalDate.of(2026, 9, 5));
        rake.setRakeStatus(StatusEnum.A);

        RakeAnnexureEntity ann = new RakeAnnexureEntity();
        ann.setRrDate(LocalDate.of(2026, 9, 5));
        ann.setRrNo("RR-999");
        ann.setWagons(50);
        rake.setAnnexures(List.of(ann));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(10L, 100L, StatusEnum.A))
                .thenReturn(List.of(assignmentA));
        when(configurationRepository.findEffectiveConfigurations(101L, LocalDate.of(2026, 9, 5), StatusEnum.A))
                .thenReturn(List.of(cfg));

        // Simulate already generated payable
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                "RAKE", "999", 101L, 1L
        )).thenReturn(true);

        List<PartyPayableEntity> payables = partyPayableService.generatePayablesForRake(rake);

        assertTrue(payables.isEmpty());
        verify(partyPayableRepository, never()).save(any(PartyPayableEntity.class));
    }

    @Test
    @DisplayName("Generate payables for Bill with Rake Based service configuration")
    void testGeneratePayablesForBill_WithRakeBasedService() {
        PartyServiceConfigurationEntity cfg = new PartyServiceConfigurationEntity();
        cfg.setId(201L);
        cfg.setPartyAssignment(assignmentA);
        cfg.setService(coveringService);
        cfg.setPaymentType(rakeBased);
        cfg.setRate(new BigDecimal("2000.00"));
        cfg.setEffectiveFrom(LocalDate.of(2026, 9, 1));
        cfg.setStatus(StatusEnum.A);

        BillEntity bill = new BillEntity();
        bill.setBillId(2L);
        bill.setBillNumber("BILL-2026-002");
        bill.setClient(client);
        bill.setSite(site);
        bill.setWorkingMonth(LocalDate.of(2026, 9, 1));
        bill.setBillAmount(new BigDecimal("2500.00"));
        bill.setPaidAmount(new BigDecimal("2500.00"));
        bill.setPaymentStatus(org.example.incentivebackend.module.transaction.bill.enums.BillPaymentStatus.PAID);

        BillAnnexureEntity ann = new BillAnnexureEntity();
        ann.setBillAnnexureId(10L);
        ann.setRrNo("RR-12345");
        ann.setWagons(40);
        bill.setAnnexures(List.of(ann));

        when(partyAssignmentRepository.findByClient_ClientIdAndSite_SiteIdAndStatus(10L, 100L, StatusEnum.A))
                .thenReturn(List.of(assignmentA));
        when(configurationRepository.findEffectiveConfigurations(eq(101L), any(LocalDate.class), eq(StatusEnum.A)))
                .thenReturn(List.of(cfg));
        when(partyPayableRepository.existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
                "BILL", "2", 101L, 1L
        )).thenReturn(false);

        when(partyPayableRepository.save(any(PartyPayableEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        List<PartyPayableEntity> payables = partyPayableService.generatePayablesForBill(bill);

        assertNotNull(payables);
        assertEquals(1, payables.size());
        PartyPayableEntity p = payables.get(0);
        assertEquals(0, new BigDecimal("2000.00").compareTo(p.getPayableAmount()));
        assertEquals("BILL", p.getSourceType());
        assertEquals("2", p.getSourceId());
        assertEquals("BILL-BILL-2026-002", p.getSourceReference());
        verify(partyPayableRepository).save(any(PartyPayableEntity.class));
    }
}
