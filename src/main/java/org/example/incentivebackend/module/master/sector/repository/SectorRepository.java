package org.example.incentivebackend.module.master.sector.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.sector.entity.SectorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectorRepository
        extends JpaRepository<SectorEntity, Long> {

    boolean existsBySectorNameIgnoreCase(String sectorName);

    boolean existsBySectorNameIgnoreCaseAndSectorIdNot(
            String sectorName,
            Long sectorId
    );

    boolean existsBySectorShortCodeIgnoreCase(String sectorShortCode);

    boolean existsBySectorShortCodeIgnoreCaseAndSectorIdNot(
            String sectorShortCode,
            Long sectorId
    );

    @Query("""
            SELECT s
            FROM SectorEntity s
            WHERE
                (
                    :search IS NULL
                    OR LOWER(s.sectorName)
                       LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND (
                    :status IS NULL
                    OR s.sectorStatus = :status
                )
            """)
    Page<SectorEntity> findAll(
            @Param("search") String search,
            @Param("status") StatusEnum status,
            Pageable pageable
    );

    List<SectorEntity> findBySectorStatus(
            StatusEnum sectorStatus
    );
}