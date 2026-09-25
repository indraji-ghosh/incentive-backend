package org.example.incentivebackend.module.transaction.partypayable.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.common.enums.PaymentStatusEnum;
import org.example.incentivebackend.module.transaction.partypayable.entity.PartyPayableEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PartyPayableRepository extends JpaRepository<PartyPayableEntity, Long>, JpaSpecificationExecutor<PartyPayableEntity> {

    boolean existsBySourceTypeAndSourceIdAndPartyAssignment_IdAndService_Id(
            String sourceType, String sourceId, Long partyAssignmentId, Long serviceId);

    boolean existsByPartyServiceConfiguration_Id(Long configId);

    List<PartyPayableEntity> findByParty_IdAndStatus(Long partyId, StatusEnum status);

    List<PartyPayableEntity> findByParty_IdAndStatusAndPaymentStatus(Long partyId, StatusEnum status, PaymentStatusEnum paymentStatus);

    List<PartyPayableEntity> findByParty_Id(Long partyId);

    List<PartyPayableEntity> findBySourceTypeAndSourceId(String sourceType, String sourceId);

    @Query("SELECT COALESCE(SUM(p.payableAmount), 0) FROM PartyPayableEntity p " +
           "WHERE p.party.id = :partyId AND p.status = :status")
    BigDecimal sumPayableAmountByPartyIdAndStatus(
            @Param("partyId") Long partyId,
            @Param("status") StatusEnum status);
}
