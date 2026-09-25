package org.example.incentivebackend.module.master.party.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.party.entity.PartyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<PartyEntity, Long>, JpaSpecificationExecutor<PartyEntity> {

    Optional<PartyEntity> findByPartyNameIgnoreCase(String partyName);

    boolean existsByPartyNameIgnoreCase(String partyName);

    boolean existsByPartyNameIgnoreCaseAndIdNot(String partyName, Long id);

    List<PartyEntity> findByPartyStatus(StatusEnum status);
}
