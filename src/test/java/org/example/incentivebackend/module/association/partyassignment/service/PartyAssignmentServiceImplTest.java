package org.example.incentivebackend.module.association.partyassignment.service;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyAssignmentRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyServiceConfigurationRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyAssignmentResponse;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.mapper.PartyAssignmentMapper;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyAssignmentRepository;
import org.example.incentivebackend.module.association.partyassignment.repository.PartyServiceConfigurationRepository;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.example.incentivebackend.module.master.client.repository.ClientRepository;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.example.incentivebackend.module.master.party.repository.PartyRepository;
import org.example.incentivebackend.module.master.paymenttype.entity.PaymentTypeEntity;
import org.example.incentivebackend.module.master.paymenttype.repository.PaymentTypeRepository;
import org.example.incentivebackend.module.master.servicetype.entity.ServiceTypeEntity;
import org.example.incentivebackend.module.master.servicetype.repository.ServiceTypeRepository;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.example.incentivebackend.module.master.site.repository.SiteRepository;
import org.example.incentivebackend.module.master.unit.entity.UnitEntity;
import org.example.incentivebackend.module.master.unit.repository.UnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartyAssignmentServiceImplTest {

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
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private PartyAssignmentMapper partyAssignmentMapper;

    @InjectMocks
    private PartyAssignmentServiceImpl partyAssignmentService;

    private PartyEntity partyA;
    private ClientEntity ultraTech;
    private ClientEntity adani;
    private SiteEntity siteA;
    private SiteEntity siteB;
    private ServiceTypeEntity coveringService;
    private ServiceTypeEntity uncoveringService;
    private PaymentTypeEntity rakeBased;
    private PaymentTypeEntity wagonBased;
    private UnitEntity rakeUnit;
    private UnitEntity wagonUnit;

    @BeforeEach
    void setUp() {
        partyA = new PartyEntity();
        partyA.setId(1L);
        partyA.setPartyName("Party A");
        partyA.setPartyStatus(StatusEnum.A);

        ultraTech = new ClientEntity();
        ultraTech.setClientId(10L);
        ultraTech.setClientName("UltraTech");
        ultraTech.setClientStatus(StatusEnum.A);

        adani = new ClientEntity();
        adani.setClientId(20L);
        adani.setClientName("Adani");
        adani.setClientStatus(StatusEnum.A);

        siteA = new SiteEntity();
        siteA.setSiteId(100L);
        siteA.setSiteName("Site A");
        siteA.setSiteStatus(StatusEnum.A);

        siteB = new SiteEntity();
        siteB.setSiteId(200L);
        siteB.setSiteName("Site B");
        siteB.setSiteStatus(StatusEnum.A);

        coveringService = new ServiceTypeEntity();
        coveringService.setId(1001L);
        coveringService.setName("Covering");

        uncoveringService = new ServiceTypeEntity();
        uncoveringService.setId(1002L);
        uncoveringService.setName("Uncovering");

        rakeBased = new PaymentTypeEntity();
        rakeBased.setId(501L);
        rakeBased.setCode("RAKE_BASED");

        wagonBased = new PaymentTypeEntity();
        wagonBased.setId(502L);
        wagonBased.setCode("WAGON_BASED");

        rakeUnit = new UnitEntity();
        rakeUnit.setId(601L);
        rakeUnit.setCode("RAKE");

        wagonUnit = new UnitEntity();
        wagonUnit.setId(602L);
        wagonUnit.setCode("WAGON");
    }

    @Test
    @DisplayName("Requirement 1: Party Assignment creation")
    void testCreatePartyAssignment_Success() {
        PartyAssignmentRequest request = new PartyAssignmentRequest();
        request.setPartyId(1L);
        request.setClientId(10L);
        request.setSiteId(100L);

        when(partyRepository.findById(1L)).thenReturn(Optional.of(partyA));
        when(clientRepository.findById(10L)).thenReturn(Optional.of(ultraTech));
        when(siteRepository.findById(100L)).thenReturn(Optional.of(siteA));
        when(partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(1L, 10L, 100L)).thenReturn(false);

        PartyAssignmentEntity savedEntity = new PartyAssignmentEntity();
        savedEntity.setId(1000L);
        savedEntity.setParty(partyA);
        savedEntity.setClient(ultraTech);
        savedEntity.setSite(siteA);
        when(partyAssignmentRepository.save(any(PartyAssignmentEntity.class))).thenReturn(savedEntity);

        PartyAssignmentResponse expectedResponse = new PartyAssignmentResponse();
        expectedResponse.setId(1000L);
        expectedResponse.setPartyName("Party A");
        expectedResponse.setClientName("UltraTech");
        expectedResponse.setSiteName("Site A");
        when(partyAssignmentMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        PartyAssignmentResponse response = partyAssignmentService.create(request);

        assertNotNull(response);
        assertEquals(1000L, response.getId());
        assertEquals("Party A", response.getPartyName());
        assertEquals("UltraTech", response.getClientName());
        assertEquals("Site A", response.getSiteName());
        verify(partyAssignmentRepository).save(any(PartyAssignmentEntity.class));
    }

    @Test
    @DisplayName("Requirement 2: Duplicate Party + Client + Site prevention")
    void testCreatePartyAssignment_Duplicate_ThrowsException() {
        PartyAssignmentRequest request = new PartyAssignmentRequest();
        request.setPartyId(1L);
        request.setClientId(10L);
        request.setSiteId(100L);

        when(partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(1L, 10L, 100L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> partyAssignmentService.create(request));
        verify(partyAssignmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Requirement 3: Multiple services for same assignment")
    void testMultipleServicesForSameAssignment() {
        PartyAssignmentRequest request = new PartyAssignmentRequest();
        request.setPartyId(1L);
        request.setClientId(10L);
        request.setSiteId(100L);

        PartyServiceConfigurationRequest cfg1 = new PartyServiceConfigurationRequest();
        cfg1.setServiceId(1001L);
        cfg1.setPaymentTypeId(501L);
        cfg1.setUnitId(601L);
        cfg1.setRate(new BigDecimal("2000.00"));
        cfg1.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        PartyServiceConfigurationRequest cfg2 = new PartyServiceConfigurationRequest();
        cfg2.setServiceId(1002L);
        cfg2.setPaymentTypeId(501L);
        cfg2.setUnitId(601L);
        cfg2.setRate(new BigDecimal("1000.00"));
        cfg2.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        request.setServiceConfigurations(List.of(cfg1, cfg2));

        when(partyRepository.findById(1L)).thenReturn(Optional.of(partyA));
        when(clientRepository.findById(10L)).thenReturn(Optional.of(ultraTech));
        when(siteRepository.findById(100L)).thenReturn(Optional.of(siteA));
        when(partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(1L, 10L, 100L)).thenReturn(false);

        when(serviceRepository.findById(1001L)).thenReturn(Optional.of(coveringService));
        when(serviceRepository.findById(1002L)).thenReturn(Optional.of(uncoveringService));
        when(paymentTypeRepository.findById(501L)).thenReturn(Optional.of(rakeBased));
        when(unitRepository.findById(601L)).thenReturn(Optional.of(rakeUnit));

        PartyAssignmentEntity savedEntity = new PartyAssignmentEntity();
        savedEntity.setId(1000L);
        savedEntity.setParty(partyA);
        savedEntity.setClient(ultraTech);
        savedEntity.setSite(siteA);

        when(partyAssignmentRepository.save(any(PartyAssignmentEntity.class))).thenAnswer(invocation -> {
            PartyAssignmentEntity entity = invocation.getArgument(0);
            entity.setId(1000L);
            return entity;
        });

        PartyAssignmentResponse responseMock = new PartyAssignmentResponse();
        responseMock.setId(1000L);
        when(partyAssignmentMapper.toResponse(any(PartyAssignmentEntity.class))).thenReturn(responseMock);

        PartyAssignmentResponse response = partyAssignmentService.create(request);

        assertNotNull(response);
        verify(partyAssignmentRepository).save(argThat(entity ->
                entity.getServiceConfigurations().size() == 2
        ));
    }

    @Test
    @DisplayName("Requirement 4: Same Party across multiple Client + Site combinations")
    void testSamePartyAcrossMultipleClientSiteCombinations() {
        // Assignment 1: Party A + UltraTech + Site A
        PartyAssignmentRequest req1 = new PartyAssignmentRequest();
        req1.setPartyId(1L);
        req1.setClientId(10L);
        req1.setSiteId(100L);

        // Assignment 2: Party A + Adani + Site B
        PartyAssignmentRequest req2 = new PartyAssignmentRequest();
        req2.setPartyId(1L);
        req2.setClientId(20L);
        req2.setSiteId(200L);

        when(partyRepository.findById(1L)).thenReturn(Optional.of(partyA));
        when(clientRepository.findById(10L)).thenReturn(Optional.of(ultraTech));
        when(siteRepository.findById(100L)).thenReturn(Optional.of(siteA));
        when(partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(1L, 10L, 100L)).thenReturn(false);

        when(clientRepository.findById(20L)).thenReturn(Optional.of(adani));
        when(siteRepository.findById(200L)).thenReturn(Optional.of(siteB));
        when(partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(1L, 20L, 200L)).thenReturn(false);

        when(partyAssignmentRepository.save(any(PartyAssignmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(partyAssignmentMapper.toResponse(any())).thenReturn(new PartyAssignmentResponse());

        PartyAssignmentResponse resp1 = partyAssignmentService.create(req1);
        PartyAssignmentResponse resp2 = partyAssignmentService.create(req2);

        assertNotNull(resp1);
        assertNotNull(resp2);
        verify(partyAssignmentRepository, times(2)).save(any(PartyAssignmentEntity.class));
    }

    @Test
    @DisplayName("Requirement 5: Different payment types for different assignments")
    void testDifferentPaymentTypesForDifferentAssignments() {
        // Assignment 1: RAKE_BASED
        PartyAssignmentRequest req1 = new PartyAssignmentRequest();
        req1.setPartyId(1L);
        req1.setClientId(10L);
        req1.setSiteId(100L);
        PartyServiceConfigurationRequest cfg1 = new PartyServiceConfigurationRequest();
        cfg1.setServiceId(1001L);
        cfg1.setPaymentTypeId(501L); // RAKE_BASED
        cfg1.setUnitId(601L);
        cfg1.setRate(new BigDecimal("2000.00"));
        cfg1.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        req1.setServiceConfigurations(List.of(cfg1));

        // Assignment 2: WAGON_BASED
        PartyAssignmentRequest req2 = new PartyAssignmentRequest();
        req2.setPartyId(1L);
        req2.setClientId(20L);
        req2.setSiteId(200L);
        PartyServiceConfigurationRequest cfg2 = new PartyServiceConfigurationRequest();
        cfg2.setServiceId(1001L);
        cfg2.setPaymentTypeId(502L); // WAGON_BASED
        cfg2.setUnitId(602L);
        cfg2.setRate(new BigDecimal("50.00"));
        cfg2.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        req2.setServiceConfigurations(List.of(cfg2));

        when(partyRepository.findById(1L)).thenReturn(Optional.of(partyA));
        when(clientRepository.findById(10L)).thenReturn(Optional.of(ultraTech));
        when(siteRepository.findById(100L)).thenReturn(Optional.of(siteA));
        when(clientRepository.findById(20L)).thenReturn(Optional.of(adani));
        when(siteRepository.findById(200L)).thenReturn(Optional.of(siteB));

        when(serviceRepository.findById(1001L)).thenReturn(Optional.of(coveringService));
        when(paymentTypeRepository.findById(501L)).thenReturn(Optional.of(rakeBased));
        when(paymentTypeRepository.findById(502L)).thenReturn(Optional.of(wagonBased));
        when(unitRepository.findById(601L)).thenReturn(Optional.of(rakeUnit));
        when(unitRepository.findById(602L)).thenReturn(Optional.of(wagonUnit));

        when(partyAssignmentRepository.save(any(PartyAssignmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(partyAssignmentMapper.toResponse(any())).thenReturn(new PartyAssignmentResponse());

        partyAssignmentService.create(req1);
        partyAssignmentService.create(req2);

        verify(partyAssignmentRepository).save(argThat(a ->
                a.getServiceConfigurations().stream().anyMatch(c -> c.getPaymentType().getCode().equals("RAKE_BASED"))
        ));
        verify(partyAssignmentRepository).save(argThat(a ->
                a.getServiceConfigurations().stream().anyMatch(c -> c.getPaymentType().getCode().equals("WAGON_BASED"))
        ));
    }
}
