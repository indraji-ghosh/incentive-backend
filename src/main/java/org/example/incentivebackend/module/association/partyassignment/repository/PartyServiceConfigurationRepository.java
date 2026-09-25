package org.example.incentivebackend.module.association.partyassignment.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.association.partyassignment.entity.PartyServiceConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PartyServiceConfigurationRepository extends JpaRepository<PartyServiceConfigurationEntity, Long> {

    List<PartyServiceConfigurationEntity> findByPartyAssignment_IdAndStatus(Long partyAssignmentId, StatusEnum status);

    @Query("SELECT sc FROM PartyServiceConfigurationEntity sc " +
           "LEFT JOIN FETCH sc.service s " +
           "LEFT JOIN FETCH sc.paymentType pt " +
           "LEFT JOIN FETCH sc.unit u " +
           "WHERE sc.partyAssignment.id = :partyAssignmentId " +
           "AND sc.status = :status " +
           "AND sc.effectiveFrom <= :transactionDate " +
           "AND (sc.effectiveTo IS NULL OR sc.effectiveTo >= :transactionDate)")
    List<PartyServiceConfigurationEntity> findEffectiveConfigurations(
            @Param("partyAssignmentId") Long partyAssignmentId,
            @Param("transactionDate") LocalDate transactionDate,
            @Param("status") StatusEnum status
    );

    @Query("SELECT sc FROM PartyServiceConfigurationEntity sc " +
           "JOIN FETCH sc.partyAssignment pa " +
           "JOIN FETCH pa.party p " +
           "JOIN FETCH pa.client c " +
           "JOIN FETCH pa.site s " +
           "JOIN FETCH sc.service srv " +
           "JOIN FETCH sc.paymentType pt " +
           "JOIN FETCH sc.unit u " +
           "WHERE (UPPER(pt.code) = 'MONTHLY_FIXED' OR UPPER(pt.code) = 'FIXED') " +
           "AND sc.status = :status " +
           "AND pa.status = :status " +
           "AND sc.effectiveFrom <= :date " +
           "AND (sc.effectiveTo IS NULL OR sc.effectiveTo >= :date)")
    List<PartyServiceConfigurationEntity> findActiveMonthlyFixedConfigurations(
            @Param("date") LocalDate date,
            @Param("status") StatusEnum status
    );
}
