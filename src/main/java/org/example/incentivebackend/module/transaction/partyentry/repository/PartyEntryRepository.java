package org.example.incentivebackend.module.transaction.partyentry.repository;

import org.example.incentivebackend.module.transaction.partyentry.entity.PartyEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyEntryRepository extends JpaRepository<PartyEntryEntity, Long>, JpaSpecificationExecutor<PartyEntryEntity> {
}
