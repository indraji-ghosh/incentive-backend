package org.example.incentivebackend.module.master.designation.repository;

import org.example.incentivebackend.module.master.designation.entity.DesignationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<DesignationEntity, Long> {
    Optional<DesignationEntity> findByDesignationCode(String designationCode);
    Optional<DesignationEntity> findByDesignationName(String designationName);
    boolean existsByDesignationCodeIgnoreCase(String designationCode);
}
