package org.example.incentivebackend.module.association.partyassignment.service;

import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyAssignmentRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.request.PartyServiceConfigurationRequest;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyAssignmentResponse;
import org.example.incentivebackend.module.association.partyassignment.dto.response.PartyServiceConfigurationResponse;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PartyAssignmentService {

    PartyAssignmentResponse create(PartyAssignmentRequest request);

    PartyAssignmentResponse update(Long id, PartyAssignmentRequest request);

    PartyAssignmentResponse findById(Long id);

    Page<PartyAssignmentResponse> findAll(Long partyId, Long clientId, Long siteId, Pageable pageable);

    List<PartyAssignmentResponse> findByClientAndSite(Long clientId, Long siteId);

    List<PartyAssignmentResponse> findByParty(Long partyId);

    void delete(Long id);

    PartyServiceConfigurationResponse addServiceConfiguration(Long assignmentId, PartyServiceConfigurationRequest request);

    PartyServiceConfigurationResponse updateServiceConfiguration(Long assignmentId, Long configId, PartyServiceConfigurationRequest request);

    void removeServiceConfiguration(Long assignmentId, Long configId);

    List<PartyServiceConfigurationEntity> resolveApplicableConfigurations(Long assignmentId, LocalDate transactionDate);
}
