package org.example.incentivebackend.module.association.partyassignment.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyAssignmentRepository extends JpaRepository<PartyAssignmentEntity, Long>, JpaSpecificationExecutor<PartyAssignmentEntity> {

    boolean existsByParty_IdAndClient_ClientIdAndSite_SiteId(Long partyId, Long clientId, Long siteId);

    boolean existsByParty_IdAndClient_ClientIdAndSite_SiteIdAndIdNot(Long partyId, Long clientId, Long siteId, Long id);

    Optional<PartyAssignmentEntity> findByParty_IdAndClient_ClientIdAndSite_SiteId(Long partyId, Long clientId, Long siteId);

    List<PartyAssignmentEntity> findByClient_ClientIdAndSite_SiteIdAndStatus(Long clientId, Long siteId, StatusEnum status);

    List<PartyAssignmentEntity> findByParty_IdAndStatus(Long partyId, StatusEnum status);

    List<PartyAssignmentEntity> findByParty_IdAndClient_ClientIdAndSite_SiteIdAndStatus(Long partyId, Long clientId, Long siteId, StatusEnum status);

    @Query("SELECT pa FROM PartyAssignmentEntity pa " +
           "JOIN FETCH pa.party p " +
           "JOIN FETCH pa.client c " +
           "JOIN FETCH pa.site s " +
           "LEFT JOIN FETCH pa.serviceConfigurations sc " +
           "WHERE pa.id = :id")
    Optional<PartyAssignmentEntity> findByIdWithDetails(@Param("id") Long id);
}
