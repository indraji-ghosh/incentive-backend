package org.example.incentivebackend.module.association.partyassignment.service;

import lombok.RequiredArgsConstructor;
import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.exception.BusinessValidationException;
import org.example.incentivebackend.common.exception.DuplicateResourceException;
import org.example.incentivebackend.common.exception.ResourceNotFoundException;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyAssignmentRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyServiceConfigurationRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyAssignmentResponse;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyServiceConfigurationResponse;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
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
import org.example.incentivebackend.module.transaction.partypayable.repository.PartyPayableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartyAssignmentServiceImpl implements PartyAssignmentService {

    private final PartyAssignmentRepository partyAssignmentRepository;
    private final PartyServiceConfigurationRepository configurationRepository;
    private final PartyPayableRepository partyPayableRepository;
    private final PartyRepository partyRepository;
    private final ClientRepository clientRepository;
    private final SiteRepository siteRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final UnitRepository unitRepository;
    private final PartyAssignmentMapper mapper;

    @Override
    public PartyAssignmentResponse create(PartyAssignmentRequest request) {
        if (partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteId(
                request.getPartyId(), request.getClientId(), request.getSiteId())) {
            throw new DuplicateResourceException("Party is already assigned to this Client and Site combination");
        }

        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + request.getPartyId()));

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        SiteEntity site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));

        PartyAssignmentEntity entity = new PartyAssignmentEntity();
        entity.setParty(party);
        entity.setClient(client);
        entity.setSite(site);
        entity.setStatus(request.getStatus() != null ? request.getStatus() : StatusEnum.A);

        if (request.getServiceConfigurations() != null) {
            for (PartyServiceConfigurationRequest configReq : request.getServiceConfigurations()) {
                PartyServiceConfigurationEntity configEntity = buildConfigurationEntity(configReq);
                entity.addServiceConfiguration(configEntity);
            }
            validateNoOverlappingConfigurations(entity.getServiceConfigurations());
        }

        PartyAssignmentEntity saved = partyAssignmentRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public PartyAssignmentResponse update(Long id, PartyAssignmentRequest request) {
        PartyAssignmentEntity entity = partyAssignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + id));

        if (partyAssignmentRepository.existsByParty_IdAndClient_ClientIdAndSite_SiteIdAndIdNot(
                request.getPartyId(), request.getClientId(), request.getSiteId(), id)) {
            throw new DuplicateResourceException("Another assignment already exists for this Party, Client, and Site");
        }

        PartyEntity party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with id: " + request.getPartyId()));

        ClientEntity client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        SiteEntity site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));

        entity.setParty(party);
        entity.setClient(client);
        entity.setSite(site);
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        if (request.getServiceConfigurations() != null) {
            List<PartyServiceConfigurationEntity> existingConfigs = entity.getServiceConfigurations();
            List<Long> requestIds = request.getServiceConfigurations().stream()
                    .map(PartyServiceConfigurationRequest::getId)
                    .filter(Objects::nonNull)
                    .toList();

            List<PartyServiceConfigurationEntity> toRemove = existingConfigs.stream()
                    .filter(c -> c.getId() != null && !requestIds.contains(c.getId()))
                    .toList();

            for (PartyServiceConfigurationEntity c : toRemove) {
                if (partyPayableRepository.existsByPartyServiceConfiguration_Id(c.getId())) {
                     throw new BusinessValidationException("Cannot delete configuration because it has already generated payables. Please expire it instead.");
                }
                entity.removeServiceConfiguration(c);
            }

            for (PartyServiceConfigurationRequest configReq : request.getServiceConfigurations()) {
                if (configReq.getId() != null) {
                    PartyServiceConfigurationEntity existing = existingConfigs.stream()
                            .filter(c -> c.getId().equals(configReq.getId()))
                            .findFirst()
                            .orElse(null);
                    if (existing != null) {
                        if (partyPayableRepository.existsByPartyServiceConfiguration_Id(existing.getId())) {
                            if (existing.getRate().compareTo(configReq.getRate()) != 0) {
                                throw new BusinessValidationException("Cannot modify the rate of configuration because it has already generated payables.");
                            }
                        }
                        existing.setService(serviceTypeRepository.findById(configReq.getServiceId())
                                .orElseThrow(() -> new ResourceNotFoundException("Service not found")));
                        existing.setPaymentType(paymentTypeRepository.findById(configReq.getPaymentTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found")));
                        existing.setUnit(unitRepository.findById(configReq.getUnitId())
                                .orElseThrow(() -> new ResourceNotFoundException("Unit not found")));
                        existing.setRate(configReq.getRate());
                        existing.setEffectiveFrom(configReq.getEffectiveFrom());
                        existing.setEffectiveTo(configReq.getEffectiveTo());
                        existing.setStatus(configReq.getStatus() != null ? configReq.getStatus() : StatusEnum.A);
                        existing.setNotes(configReq.getNotes());
                    }
                } else {
                    PartyServiceConfigurationEntity configEntity = buildConfigurationEntity(configReq);
                    entity.addServiceConfiguration(configEntity);
                }
            }
            validateNoOverlappingConfigurations(entity.getServiceConfigurations());
        }

        PartyAssignmentEntity updated = partyAssignmentRepository.save(entity);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PartyAssignmentResponse findById(Long id) {
        PartyAssignmentEntity entity = partyAssignmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartyAssignmentResponse> findAll(Long partyId, Long clientId, Long siteId, Pageable pageable) {
        Specification<PartyAssignmentEntity> spec = (root, query, cb) -> cb.conjunction();
        if (partyId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("party").get("id"), partyId));
        }
        if (clientId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("client").get("clientId"), clientId));
        }
        if (siteId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("site").get("siteId"), siteId));
        }
        return partyAssignmentRepository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyAssignmentResponse> findByClientAndSite(Long clientId, Long siteId) {
        List<PartyAssignmentEntity> list = partyAssignmentRepository
                .findByClient_ClientIdAndSite_SiteIdAndStatus(clientId, siteId, StatusEnum.A);
        return mapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyAssignmentResponse> findByParty(Long partyId) {
        List<PartyAssignmentEntity> list = partyAssignmentRepository.findByParty_IdAndStatus(partyId, StatusEnum.A);
        return mapper.toResponseList(list);
    }

    @Override
    public void delete(Long id) {
        PartyAssignmentEntity entity = partyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + id));
        partyAssignmentRepository.delete(entity);
    }

    @Override
    public PartyServiceConfigurationResponse addServiceConfiguration(Long assignmentId, PartyServiceConfigurationRequest request) {
        PartyAssignmentEntity assignment = partyAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + assignmentId));

        PartyServiceConfigurationEntity configEntity = buildConfigurationEntity(request);
        assignment.addServiceConfiguration(configEntity);
        validateNoOverlappingConfigurations(assignment.getServiceConfigurations());

        PartyServiceConfigurationEntity saved = configurationRepository.save(configEntity);
        return mapper.toServiceConfigResponse(saved);
    }

    @Override
    public PartyServiceConfigurationResponse updateServiceConfiguration(Long assignmentId, Long configId, PartyServiceConfigurationRequest request) {
        PartyAssignmentEntity assignment = partyAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + assignmentId));

        PartyServiceConfigurationEntity configEntity = configurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + configId));

        if (!Objects.equals(configEntity.getPartyAssignment().getId(), assignmentId)) {
            throw new BusinessValidationException("Configuration does not belong to the specified Party Assignment");
        }

        validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());

        ServiceTypeEntity service = serviceTypeRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + request.getServiceId()));
        PaymentTypeEntity paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + request.getPaymentTypeId()));
        UnitEntity unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getUnitId()));

        if (partyPayableRepository.existsByPartyServiceConfiguration_Id(configId)) {
            if (configEntity.getRate().compareTo(request.getRate()) != 0) {
                throw new BusinessValidationException("Cannot modify the rate of this configuration because it has already been used to generate payables. Please expire this configuration and create a new one instead.");
            }
        }

        configEntity.setService(service);
        configEntity.setPaymentType(paymentType);
        configEntity.setUnit(unit);
        configEntity.setRate(request.getRate());
        configEntity.setEffectiveFrom(request.getEffectiveFrom());
        configEntity.setEffectiveTo(request.getEffectiveTo());
        configEntity.setStatus(request.getStatus() != null ? request.getStatus() : StatusEnum.A);
        configEntity.setNotes(request.getNotes());

        validateNoOverlappingConfigurations(assignment.getServiceConfigurations());

        PartyServiceConfigurationEntity updated = configurationRepository.save(configEntity);
        return mapper.toServiceConfigResponse(updated);
    }

    @Override
    public void removeServiceConfiguration(Long assignmentId, Long configId) {
        PartyAssignmentEntity assignment = partyAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Party Assignment not found with id: " + assignmentId));

        PartyServiceConfigurationEntity configEntity = configurationRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + configId));

        assignment.removeServiceConfiguration(configEntity);
        configurationRepository.delete(configEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartyServiceConfigurationEntity> resolveApplicableConfigurations(Long assignmentId, LocalDate transactionDate) {
        return configurationRepository.findEffectiveConfigurations(assignmentId, transactionDate, StatusEnum.A);
    }

    private PartyServiceConfigurationEntity buildConfigurationEntity(PartyServiceConfigurationRequest request) {
        validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());

        ServiceTypeEntity service = serviceTypeRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + request.getServiceId()));

        PaymentTypeEntity paymentType = paymentTypeRepository.findById(request.getPaymentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment Type not found: " + request.getPaymentTypeId()));

        UnitEntity unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + request.getUnitId()));

        PartyServiceConfigurationEntity entity = new PartyServiceConfigurationEntity();
        entity.setService(service);
        entity.setPaymentType(paymentType);
        entity.setUnit(unit);
        entity.setRate(request.getRate());
        entity.setEffectiveFrom(request.getEffectiveFrom());
        entity.setEffectiveTo(request.getEffectiveTo());
        entity.setStatus(request.getStatus() != null ? request.getStatus() : StatusEnum.A);
        entity.setNotes(request.getNotes());
        return entity;
    }

    private void validateEffectiveDates(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new BusinessValidationException("Effective from date cannot be null");
        }
        if (to != null && to.isBefore(from)) {
            throw new BusinessValidationException("Effective To date cannot be before Effective From date");
        }
    }

    private void validateNoOverlappingConfigurations(List<PartyServiceConfigurationEntity> configurations) {
        if (configurations == null || configurations.size() < 2) {
            return;
        }

        for (int i = 0; i < configurations.size(); i++) {
            PartyServiceConfigurationEntity c1 = configurations.get(i);
            for (int j = i + 1; j < configurations.size(); j++) {
                PartyServiceConfigurationEntity c2 = configurations.get(j);

                if (Objects.equals(c1.getService().getId(), c2.getService().getId()) &&
                    Objects.equals(c1.getPaymentType().getId(), c2.getPaymentType().getId())) {

                    boolean overlaps = (c1.getEffectiveTo() == null || !c1.getEffectiveTo().isBefore(c2.getEffectiveFrom())) &&
                                       (c2.getEffectiveTo() == null || !c2.getEffectiveTo().isBefore(c1.getEffectiveFrom()));

                    if (overlaps) {
                        throw new BusinessValidationException("Overlapping effective date periods detected for Service '"
                                + c1.getService().getName() + "' and Payment Type '" + c1.getPaymentType().getName() + "'");
                    }
                }
            }
        }
    }
}
