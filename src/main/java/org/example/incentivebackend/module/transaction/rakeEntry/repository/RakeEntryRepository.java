package org.example.incentivebackend.module.transaction.rakeEntry.repository;

import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RakeEntryRepository extends JpaRepository<RakeEntryEntity, Long> {
    List<RakeEntryEntity> findByClient_ClientId(Long clientId);

    List<RakeEntryEntity> findByParty_Id(Long partyId);

    List<RakeEntryEntity> findByWorkingMonth(LocalDate workingMonth);

    List<RakeEntryEntity> findByParty_IdAndClient_ClientIdAndWorkingMonthBetween(Long partyId, Long clientId, LocalDate startDate, LocalDate endDate);

    List<RakeEntryEntity> findByParty_IdAndWorkingMonthBetween(Long partyId, LocalDate startDate, LocalDate endDate);
}
