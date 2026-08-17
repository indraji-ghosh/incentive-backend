package org.example.incentivebackend.module.master.client.repository;


import org.example.incentivebackend.common.enums.StatusEnum;
import org.example.incentivebackend.module.master.client.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository
        extends JpaRepository<ClientEntity, Long> {

    boolean existsByClientShortCode(String clientShortCode);

    boolean existsByClientShortCodeAndClientIdNot(
            String clientShortCode,
            Long clientId
    );

    Optional<ClientEntity> findByClientShortCode(
            String clientShortCode
    );

    @Query("""
            SELECT c
            FROM ClientEntity c
            WHERE
                (
                    :search IS NULL
                    OR LOWER(c.clientName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.clientShortCode) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            AND (
                    :status IS NULL
                    OR c.clientStatus = :status
                )
            """)
    Page<ClientEntity> findAll(
            @Param("search") String search,
            @Param("status") StatusEnum status,
            Pageable pageable
    );

    java.util.List<ClientEntity> findByClientStatus(
            StatusEnum clientStatus
    );
}
