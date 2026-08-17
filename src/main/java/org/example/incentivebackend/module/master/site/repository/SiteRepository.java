package org.example.incentivebackend.module.master.site.repository;

import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.site.entity.SiteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteRepository
        extends JpaRepository<SiteEntity, Long> {

    boolean existsBySiteShortCodeIgnoreCase(
            String siteShortCode
    );

    boolean existsBySiteShortCodeIgnoreCaseAndSiteIdNot(
            String siteShortCode,
            Long siteId
    );

    @Query("""
            SELECT s
            FROM SiteEntity s
            WHERE
                (
                    :search IS NULL
                    OR LOWER(s.siteName)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.siteShortCode)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(s.state)
                        LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND (
                    :state IS NULL
                    OR LOWER(s.state)
                        = LOWER(:state)
                )
            AND (
                    :status IS NULL
                    OR s.siteStatus = :status
                )
            """)
    Page<SiteEntity> findAll(
            @Param("search") String search,
            @Param("state") String state,
            @Param("status") StatusEnum status,
            Pageable pageable
    );

    List<SiteEntity> findBySiteStatus(
            StatusEnum siteStatus
    );
}