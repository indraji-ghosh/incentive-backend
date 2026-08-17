package org.example.incentivebackend.module.master.businessHead.repository;

import org.example.incentivebackend.module.master.businessHead.entity.BusinessHeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessHeadRepository
        extends JpaRepository<BusinessHeadEntity, Long> {

    Optional<BusinessHeadEntity> findByHeadShortCode(String headShortCode);

    boolean existsByHeadShortCode(String headShortCode);
}