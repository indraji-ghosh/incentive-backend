package org.example.incentivebackend.module.transaction.rakeEntry.repository;

import org.example.incentivebackend.module.transaction.rakeEntry.entity.RakeAnnexureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RakeAnnexureRepository extends JpaRepository<RakeAnnexureEntity, Long> {
    List<RakeAnnexureEntity> findByRakeEntry_RakeEntryId(Long rakeEntryId);
}
